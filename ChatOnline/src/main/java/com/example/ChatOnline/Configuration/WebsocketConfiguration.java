package com.example.ChatOnline.Configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer { //Định nghĩa endpoint để clients connect
    private final WebsocketHandshake websocketHandshake;
    private final ClientInboundAuthentication authentication;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // cho phép frontend kết nối tới backend
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .addInterceptors(websocketHandshake); // để validate connection như việc ktra token ... trước khi được upgrade lên Websocket
    }

        @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        // /topic : đăng kí user trong cuộc hội thoại như nhóm hoặc broadcast(1 - N)
            // /queue : (1 - 1) (Private conversation)
            registry.enableSimpleBroker("/topic", "/queue");

            // Prefix cho messages từ client → server
            // Client gửi đến /app/xxx → @MessageMapping("/xxx") xử lý
            registry.setApplicationDestinationPrefixes("/app");

            // Prefix cho user-specific destinations
            // Cho phép gửi messages đến specific user session.
            // Mỗi user session có unique destination, đảm bảo message chỉ đến đúng người.
            registry.setUserDestinationPrefix("/user");
        }

        @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
            // Register ChannelInterceptor để authenticate STOMP CONNECT frames
            registration.interceptors(authentication);
        }
}
