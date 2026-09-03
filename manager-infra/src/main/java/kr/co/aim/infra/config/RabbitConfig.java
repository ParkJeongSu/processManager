package kr.co.aim.infra.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Getter
@Slf4j
@Profile({"manager"})
public class RabbitConfig {

    // --- Public Static 상수 (외부 참조용) ---
    public static String EXCHANGE_DEAD;
    public static String EXCHANGE_MANAGER;

    public static String QUEUE_DEAD;
    public static String QUEUE_MANAGER;

    public static String ROUTING_DEAD;
    public static String ROUTING_MANAGER;

    public static final String DLX_KEY = "x-dead-letter-exchange";
    public static final String DLK_KEY = "x-dead-letter-routing-key";

    // --- Setter 주입 (Static 필드 할당) ---
    @Value("${custom.rabbitmq.exchange.dead}") public void setExDead(String v) { EXCHANGE_DEAD = v; }
    @Value("${custom.rabbitmq.exchange.manager}") public void setExManager(String v) { EXCHANGE_MANAGER = v; }

    @Value("${custom.rabbitmq.queue.dead}") public void setQd(String v) { QUEUE_DEAD = v; }
    @Value("${custom.rabbitmq.queue.manager}") public void setQueueManager(String v) { QUEUE_MANAGER = v; }

    @Value("${custom.rabbitmq.routing.dead}") public void setRd(String v) { ROUTING_DEAD = v; }
    @Value("${custom.rabbitmq.routing.manager}") public void setRoutingManager(String v) { ROUTING_MANAGER = v; }

    // --- RabbitAdmin 인프라 초기화 ---
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        try {
            admin.initialize();
            log.info(">>> [RabbitAdmin] All Infra (PEX, TEX, EAS, WMS, WCS, MANTI) initialized.");
        } catch (Exception e) {
            log.error(">>> [RabbitAdmin] Initialization failed: " + e.getMessage());
        }
        return admin;
    }

    private Map<String, Object> queueArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put(DLX_KEY, EXCHANGE_DEAD);
        args.put(DLK_KEY, ROUTING_DEAD);
        args.put("x-message-ttl", 600000); // 10분 (600,000ms)
        return args;
    }

    // Queue
    @Bean public Queue managerQueue() { return new Queue(QUEUE_MANAGER, true, false, false,queueArgs()); }

    // Exchanges
    @Bean public DirectExchange managerExchange() { return new DirectExchange(EXCHANGE_MANAGER); } // rpc.exchange

    // Bindings
    @Bean Binding managerBinding() { return BindingBuilder.bind(managerQueue()).to(managerExchange()).with(ROUTING_MANAGER); }

    // --- Template & Converter ---
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setReplyTimeout(60000);
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}