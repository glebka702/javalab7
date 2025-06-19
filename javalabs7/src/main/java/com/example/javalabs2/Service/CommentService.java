package com.example.javalabs2.Service;

import com.example.javalabs2.Entity.Comment;
import com.example.javalabs2.Repository.CommentRepository;
import com.example.javalabs2.Cache.CommentCache;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentCache commentCache;
    private final RequestCounter requestCounter;

    public CommentService(CommentRepository commentRepository,
                          CommentCache commentCache,
                          RequestCounter requestCounter) {
        this.commentRepository = commentRepository;
        this.commentCache = commentCache;
        this.requestCounter = requestCounter;
    }

    public Comment createComment(Comment comment) {
        requestCounter.increment();
        Comment savedComment = commentRepository.save(comment);
        commentCache.putComment(savedComment);
        return savedComment;
    }

    public List<Comment> getAllComments() {
        requestCounter.increment();
        List<Comment> comments = commentRepository.findAll();
        comments.forEach(commentCache::putComment);
        return comments;
    }

    public Comment getCommentById(Long id) {
        requestCounter.increment();
        Comment cachedComment = commentCache.getCommentById(id);
        if (cachedComment != null) {
            return cachedComment;
        }
        Optional<Comment> comment = commentRepository.findById(id);
        return comment.map(c -> {
            commentCache.putComment(c);
            return c;
        }).orElse(null);
    }

    public Comment updateComment(Long id, Comment commentDetails) {
        requestCounter.increment();
        Optional<Comment> optionalComment = commentRepository.findById(id);
        if (optionalComment.isEmpty()) {
            return null;
        }
        Comment existingComment = optionalComment.get();
        existingComment.setAuthor(commentDetails.getAuthor());
        existingComment.setText(commentDetails.getText());
        Comment updatedComment = commentRepository.save(existingComment);
        commentCache.putComment(updatedComment);
        return updatedComment;
    }

    public boolean deleteComment(Long id) {
        requestCounter.increment();
        Optional<Comment> optionalComment = commentRepository.findById(id);
        if (optionalComment.isEmpty()) {
            return false;
        }
        commentRepository.deleteById(id);
        commentCache.removeComment(id);
        return true;
    }

    public List<Comment> searchComments(Long articleId, String authorFilter) {
        requestCounter.increment();
        List<Comment> cachedComments = commentCache.getCommentsByAuthor(authorFilter.toLowerCase());
        if (!cachedComments.isEmpty() && cachedComments.stream().allMatch(
                c -> c.getArticle() != null && c.getArticle().getId().equals(articleId))) {
            return cachedComments;
        }
        List<Comment> comments = commentRepository.findByArticleIdAndAuthorContaining(articleId, authorFilter.toLowerCase());
        comments.forEach(commentCache::putComment);
        return comments;
    }
}