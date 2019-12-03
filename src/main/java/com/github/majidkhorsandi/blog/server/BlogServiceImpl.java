package com.github.majidkhorsandi.blog.server;

import com.mongodb.Block;
import com.mongodb.client.*;
import com.mongodb.client.result.UpdateResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

  private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
  private MongoDatabase database = mongoClient.getDatabase("mydb");
  private MongoCollection<Document> collection = database.getCollection("blog");

  @Override
  public void createBlog(
      CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
    Blog blog = request.getBlog();

    Document document =
        new Document("author_id", blog.getAuthorId())
            .append("title", blog.getTitle())
            .append("content", blog.getContent());

    collection.insertOne(document);
    // get mongodb generate id
    String id = document.getObjectId("_id").toString();
    CreateBlogResponse response =
        CreateBlogResponse.newBuilder().setBlog(blog.toBuilder().setId(id)).build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
    String blogId = request.getId();

    System.out.println("Looking up the blog with id: " + request.getId());
    Document result = collection.find(eq("_id", new ObjectId(blogId))).first();

    if (result == null) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription("The blog with the corresponding id was not found")
              .asRuntimeException());
    } else {
      responseObserver.onNext(buildBlogResponse(result));
    }
    responseObserver.onCompleted();
  }

  @Override
  public void readAllBlogs(
      ReadAllBlogsRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
    System.out.println("Looking up all the blog ...");

    FindIterable<Document> result = collection.find();
    result
        .cursor()
        .forEachRemaining(
            r -> {
              responseObserver.onNext(buildBlogResponse(r));
            });

    responseObserver.onCompleted();
  }

  @Override
  public void updateBlog(
      UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
    Blog blog = request.getBlog();
    String blogId = blog.getId();

    Document result = collection.find(eq("_id", new ObjectId(blogId))).first();
    if (result == null) {
      responseObserver.onError(
          Status.NOT_FOUND
              .withDescription("The blog with the corresponding id was not found")
              .asRuntimeException());
      return;
    }

    Document update = new Document();
    update.append("author_id", blog.getAuthorId());
    update.append("title", blog.getTitle());
    update.append("content", blog.getContent());
    UpdateResult updateResult = collection.replaceOne(eq("_id", new ObjectId(blogId)), update);

    if (updateResult.wasAcknowledged()) {
        System.out.println("Successfully updated the blog");
      responseObserver.onNext(UpdateBlogResponse.newBuilder().setBlog(blog).build());
      responseObserver.onCompleted();
    } else {
      responseObserver.onError(
          Status.ABORTED
              .withDescription("The blog update was not completed successfully")
              .asRuntimeException());
    }
  }

  private ReadBlogResponse buildBlogResponse(Document result) {
    return ReadBlogResponse.newBuilder()
        .setBlog(
            Blog.newBuilder()
                .setAuthorId(result.getString("author_id"))
                .setTitle(result.getString("title"))
                .setContent(result.getString("content"))
                .build())
        .build();
  }
}
