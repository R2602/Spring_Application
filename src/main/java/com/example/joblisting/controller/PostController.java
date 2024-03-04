package com.example.joblisting.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.joblisting.model.Post;
import com.example.joblisting.postRepository.PostRepository;
import com.example.joblisting.postRepository.SearchRepository;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@CrossOrigin(origins = "https://65e60e45a7d57300087f04fc--springupjobsearch.netlify.app/")
public class PostController {
	
	@Autowired
	PostRepository repo;
	@Autowired
	SearchRepository find;
	
	@ApiIgnore
	@RequestMapping(value = "/")
	public void redirect(HttpServletResponse response) throws IOException {
		response.sendRedirect("/swagger-ui.html");
	}
	@GetMapping(value = "/posts")
	@CrossOrigin
	public List<Post> getAllPosts(){
		return repo.findAll();
		
	}
	
	@GetMapping(value = "/posts/{text}")
	@CrossOrigin
	public List<Post> search(@PathVariable String text){
		return find.findByText(text);
		
	}
	
	@PostMapping(value = "/post")
	@CrossOrigin
	public Post addPost(@RequestBody Post post) {
		return repo.save(post);
	}
}
