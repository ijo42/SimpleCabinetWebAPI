package pro.gravit.simplecabinet.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pro.gravit.simplecabinet.web.dto.NewsCommentDto;
import pro.gravit.simplecabinet.web.dto.NewsDto;
import pro.gravit.simplecabinet.web.dto.PageDto;
import pro.gravit.simplecabinet.web.exception.EntityNotFoundException;
import pro.gravit.simplecabinet.web.model.News;
import pro.gravit.simplecabinet.web.model.NewsComment;
import pro.gravit.simplecabinet.web.service.NewsService;
import pro.gravit.simplecabinet.web.service.UserService;

@RestController
@RequestMapping("/news")
public class NewsController {
    @Autowired
    private NewsService newsService;
    @Autowired
    private UserService userService;

    @GetMapping("/page/{pageId}")
    public PageDto<NewsDto> getPage(@PathVariable int pageId) {
        var list = newsService.findAll(PageRequest.of(pageId, 10, Sort.Direction.ASC));
        return new PageDto<>(list.map(NewsDto::makeMiniNews));
    }

    @GetMapping("/id/{newsId}")
    public NewsDto getById(@PathVariable long newsId) {
        var news = newsService.findByIdFetchComments(newsId);
        if (news.isEmpty()) {
            throw new EntityNotFoundException("News not found");
        }
        return NewsDto.makeFullNews(news.get());
    }

    @PostMapping("/id/{newsId}/update")
    public void updateById(@PathVariable long newsId, @RequestBody NewsCreateRequest request) {
        var newsOptional = newsService.findByIdFetchComments(newsId);
        if (newsOptional.isEmpty()) {
            throw new EntityNotFoundException("News not found");
        }
        var news = newsOptional.get();
        news.setHeader(request.header);
        news.setMiniText(request.miniText);
        news.setText(request.text);
        newsService.save(news);
    }

    @DeleteMapping("/id/{newsId}/comment/{commentId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteCommentById(@PathVariable long newsId, @PathVariable long commentId) {
        var commentOptional = newsService.findCommentById(commentId);
        if (commentOptional.isEmpty()) {
            throw new EntityNotFoundException("NewsComment not found");
        }
        newsService.delete(commentOptional.get());
    }

    @PutMapping("/id/{newsId}/newcomment")
    @PreAuthorize("isAuthenticated()")
    public NewsCommentDto createComment(@PathVariable long newsId, @RequestBody NewsCommentCreateRequest createCommentRequest) {
        var news = newsService.findById(newsId);
        if (news.isEmpty()) {
            throw new EntityNotFoundException("News not found");
        }
        var comment = new NewsComment();
        comment.setUser(userService.getCurrentUser().getReference());
        comment.setText(createCommentRequest.text);
        newsService.save(comment);
        return new NewsCommentDto(comment);
    }

    @PutMapping("/new")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public NewsDto create(@RequestBody NewsCreateRequest request) {
        var news = new News();
        news.setHeader(request.header);
        news.setMiniText(request.miniText);
        news.setText(request.text);
        newsService.save(news);
        return NewsDto.makeMiniNews(news);
    }

    public record NewsCreateRequest(String header, String miniText, String text) {
    }

    public record NewsCommentCreateRequest(String text) {

    }
}
