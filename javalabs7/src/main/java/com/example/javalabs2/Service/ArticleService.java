package com.example.javalabs2.Service;

import com.example.javalabs2.Entity.Article;
import com.example.javalabs2.Entity.Comment;
import com.example.javalabs2.Repository.ArticleRepository;
import com.example.javalabs2.Repository.CommentRepository;
import com.example.javalabs2.Cache.ArticleCache;
import com.example.javalabs2.Cache.CommentCache;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ArticleCache articleCache;
    private final CommentCache commentCache;
    private final RequestCounter requestCounter;

    public ArticleService(ArticleRepository articleRepository,
                          CommentRepository commentRepository,
                          ArticleCache articleCache,
                          CommentCache commentCache,
                          RequestCounter requestCounter) {
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.articleCache = articleCache;
        this.commentCache = commentCache;
        this.requestCounter = requestCounter;
    }

    public Article createArticle(Article article) {
        requestCounter.increment();
        Article savedArticle = articleRepository.save(article);
        articleCache.putArticle(savedArticle);
        return savedArticle;
    }

    public List<Article> createArticles(List<Article> articles) {
        requestCounter.increment();
        if (articles == null || articles.isEmpty()) {
            throw new IllegalArgumentException("Article list cannot be null or empty");
        }
        List<Article> savedArticles = articleRepository.saveAll(articles);
        savedArticles.forEach(articleCache::putArticle);
        return savedArticles;
    }

    public List<Article> getAllArticles() {
        requestCounter.increment();
        List<Article> articles = articleRepository.findAll();
        articles.forEach(articleCache::putArticle);
        return articles;
    }

    public Article getArticleById(Long id) {
        requestCounter.increment();
        Article cachedArticle = articleCache.getArticleById(id);
        if (cachedArticle != null) {
            return cachedArticle;
        }
        Optional<Article> article = articleRepository.findById(id);
        return article.map(a -> {
            articleCache.putArticle(a);
            return a;
        }).orElse(null);
    }

    public Article updateArticle(Long id, Article articleDetails) {
        requestCounter.increment();
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isEmpty()) {
            return null;
        }
        Article existingArticle = optionalArticle.get();
        existingArticle.setTitle(articleDetails.getTitle());
        existingArticle.setContent(articleDetails.getContent());
        Article updatedArticle = articleRepository.save(existingArticle);
        articleCache.putArticle(updatedArticle);
        return updatedArticle;
    }

    public boolean deleteArticle(Long id) {
        requestCounter.increment();
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isEmpty()) {
            return false;
        }
        Article article = optionalArticle.get();
        List<Comment> comments = commentCache.getCommentsByArticle(id);
        if (comments.isEmpty()) {
            comments = commentRepository.findByArticleId(id);
        }
        comments.forEach(comment -> commentCache.removeComment(comment.getId()));
        articleRepository.delete(article);
        articleCache.removeArticle(id);
        return true;
    }

    public Comment addComment(Long articleId, Comment comment) {
        requestCounter.increment();
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        if (optionalArticle.isEmpty()) {
            return null;
        }
        Article article = optionalArticle.get();
        comment.setArticle(article);
        if (article.getComments() == null) {
            article.setComments(new ArrayList<>());
        }
        article.getComments().add(comment);
        Comment savedComment = commentRepository.save(comment);
        commentCache.putComment(savedComment);
        articleCache.putArticle(article);
        return savedComment;
    }

    public List<Comment> getArticleComments(Long articleId) {
        requestCounter.increment();
        List<Comment> cachedComments = commentCache.getCommentsByArticle(articleId);
        if (!cachedComments.isEmpty()) {
            return cachedComments;
        }
        List<Comment> comments = commentRepository.findByArticleId(articleId);
        comments.forEach(commentCache::putComment);
        return comments;
    }
}