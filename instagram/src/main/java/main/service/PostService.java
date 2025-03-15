package main.service;

import lombok.RequiredArgsConstructor;
import main.entity.Post;
import main.entity.User;
import main.repository.IPostRepository;
import main.repository.IUserRepository;
import main.service.dto.PostDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final IPostRepository postRepository;
    private final IUserRepository userRepository;

    public List<PostDTO> getAll() {
        return postRepository.findAll()
            .stream()
            .map(PostDTO::withRelationships)
            .collect(Collectors.toList());
    }

    public PostDTO createComment(PostDTO request) {
        // TODO: Implement validation for each edge case

        Long authorId = request.getRelationships().getAuthor().getId();
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + authorId));

        Post parent = null;
        if (request.getRelationships().getPost() != null) {
            Long postId = request.getRelationships().getPost().getId();
            parent = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found with ID: " + postId));
        }

        Post post = new Post();
        post.setTitle(request.getAttributes().getTitle());
        post.setText(request.getAttributes().getText());
        post.setImagePath(request.getAttributes().getImagePath());
        post.setAuthor(author);
        post.setParent(parent);

        return PostDTO.withRelationships(postRepository.save(post));
    }

    public PostDTO get(Long id) {
        return postRepository.findById(id)
                .map(PostDTO::withRelationships)
                .orElseThrow(() -> new RuntimeException("Post not found with ID: " + id));
    }

    public Post updateComment(Long id, String newText) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setText(newText);
        return postRepository.save(post);
    }

    public void delete(Long id) {
        postRepository.deleteById(id);
    }
}
