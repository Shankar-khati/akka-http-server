package com.akka.httptest;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;

import java.util.concurrent.CompletionStage;

public class MainClass extends AllDirectives {
    public static void main(String[] args) {
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes");
        final Http http = Http.get(Adapter.toClassic(system));

        MainClass mainClass = new MainClass();
        final Materializer materializer = Materializer.matFromSystem(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = mainClass.createRoute().flow(Adapter.toClassic(system), materializer);
        CompletionStage<ServerBinding> futureBinding =
                http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);
        try {
            system.log().info("Server online at http://localhost:8080/\nPress Enter to stop...");
            System.in.read(); // let it run until user presses return

        } catch ( Exception e) {}

        futureBinding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());


    }

    private Route createRoute() {
        return concat(
                path("hello", () ->
                        get(() ->
                                complete("Hello endpoint replied successfully.."))));
    }
}
