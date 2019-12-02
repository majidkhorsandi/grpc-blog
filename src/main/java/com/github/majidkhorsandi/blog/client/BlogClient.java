package com.github.majidkhorsandi.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Iterator;

public class BlogClient {

  public static void main(String[] args) {
    ManagedChannel channel =
        ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

    BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);

      //createBlog(stub);
      //readBlog(stub);
      readAllBlogs(stub);

  }

  private static void readAllBlogs(BlogServiceGrpc.BlogServiceBlockingStub stub) {
    Iterator<ReadBlogResponse> readBlogResponseIterator =
            stub.readAllBlogs(ReadAllBlogsRequest.newBuilder().build());
    readBlogResponseIterator.forEachRemaining(System.out::println);
  }

  private static void createBlog(BlogServiceGrpc.BlogServiceBlockingStub stub) {
    Blog blog =
        Blog.newBuilder()
            .setAuthorId("majid")
            .setContent("Hello. This is my first post")
            .setTitle("First Post")
            .build();

    CreateBlogRequest request = CreateBlogRequest.newBuilder().setBlog(blog).build();
    CreateBlogResponse blogResponse = stub.createBlog(request);
    System.out.println("created a new blog with id: " + blogResponse.getBlog().getId());
    System.out.println("created a new blog with title: " + blogResponse.getBlog().getTitle());
  }

  private static void readBlog(BlogServiceGrpc.BlogServiceBlockingStub stub) {
    ReadBlogRequest request =
        ReadBlogRequest.newBuilder().setId("5de583479769240580d38037").build();

      ReadBlogResponse readBlogResponse = stub.readBlog(request);
      System.out.println("Returned blog with id: " + readBlogResponse.getBlog().getId());
      System.out.println("Blog title: " + readBlogResponse.getBlog().getTitle());
      System.out.println("Blog author: " + readBlogResponse.getBlog().getAuthorId());
      System.out.println("Blog content: " + readBlogResponse.getBlog().getContent());
  }
}
