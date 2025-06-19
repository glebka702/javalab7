const API_BASE_URL = 'http://localhost:8080/api';

document.addEventListener('DOMContentLoaded', () => {
    loadArticles();
    loadComments();
    document.getElementById('article-form').addEventListener('submit', addArticle);
    document.getElementById('comment-form').addEventListener('submit', addComment);
});

async function loadArticles() {
    try {
        const response = await fetch(`${API_BASE_URL}/articles`);
        const articles = await response.json();
        const list = document.getElementById('articles-list');
        list.innerHTML = '';
        articles.forEach(article => {
            list.innerHTML += `
                <div>
                    ID: ${article.id}<br>
                    Title: ${article.title}<br>
                    Content: ${article.content}<br>
                    Comments: ${article.comments ? article.comments.length : 0}<br><br>
                </div>
            `;
        });
    } catch (error) {
        console.error('Error:', error);
    }
}

async function loadComments() {
    try {
        const response = await fetch(`${API_BASE_URL}/articles`);
        const articles = await response.json();
        const list = document.getElementById('comments-list');
        list.innerHTML = '';
        articles.forEach(article => {
            if (article.comments && article.comments.length > 0) {
                article.comments.forEach(comment => {
                    list.innerHTML += `
                        <div>
                            ID: ${comment.id}<br>
                            Article ID: ${article.id}<br>
                            Author: ${comment.author}<br>
                            Text: ${comment.text}<br><br>
                        </div>
                    `;
                });
            }
        });
    } catch (error) {
        console.error('Error:', error);
    }
}

async function addArticle(event) {
    event.preventDefault();
    const title = document.getElementById('article-title').value;
    const content = document.getElementById('article-content').value;
    try {
        await fetch(`${API_BASE_URL}/articles`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, content })
        });
        document.getElementById('article-form').reset();
        loadArticles();
        loadComments();
    } catch (error) {
        console.error('Error:', error);
    }
}

async function addComment(event) {
    event.preventDefault();
    const articleId = document.getElementById('comment-article-id').value;
    const author = document.getElementById('comment-author').value;
    const text = document.getElementById('comment-text').value;
    try {
        await fetch(`${API_BASE_URL}/articles/${articleId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ author, text })
        });
        document.getElementById('comment-form').reset();
        loadComments();
    } catch (error) {
        console.error('Error:', error);
    }
}