package com.github.majidkhorsandi.blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogServiceGrpc;
import com.proto.blog.CreateBlogRequest;
import com.proto.blog.CreateBlogResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

  public static void main(String[] args) {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
            .usePlaintext()
            .build();

    BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);

    Blog blog = Blog.newBuilder()
            .setAuthorId("majid")
            .setContent("Hello. This is my first post")
            .setTitle("First Post")
            .build();

    CreateBlogRequest request = CreateBlogRequest.newBuilder()
            .setBlog(blog)
            .build();

    CreateBlogResponse blogResponse = stub.createBlog(request);

    System.out.println("created a new blog with id: " + blogResponse.getBlog().getId());
    System.out.println("created a new blog with title: " + blogResponse.getBlog().getTitle());

  }
}
