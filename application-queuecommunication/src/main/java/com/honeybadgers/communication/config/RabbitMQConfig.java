package com.honeybadgers.communication.config;

import com.honeybadgers.communication.RabbitMQReceiver;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.honeybadgers.communication")
public class RabbitMQConfig {

    @Value("${dispatch.rabbitmq.dispatcherqueue}")
    String dispatcherqueue;

    @Value("${dispatch.rabbitmq.feedbackqueue}")
    String feedbackqueue;

    @Value("${dispatch.rabbitmq.tasksqueue}")
    String tasksqueue;

    @Value("${dispatch.rabbitmq.priorityqueue}")
    String priorityqueue;

    @Value("${dispatch.rabbitmq.dispatcherexchange}")
    String dispatcherExchange;

    @Value("${dispatch.rabbitmq.feedbackexchange}")
    String feedbackExchange;

    @Value("${dispatch.rabbitmq.tasksexchange}")
    String tasksExchange;
    @Value("${dispatch.rabbitmq.priorityexchange}")
    String priorityExchange;

    @Value("${dispatch.rabbitmq.dispatcherroutingkey}")
    private String dispatcherroutingkey;
    @Value("${dispatch.rabbitmq.feedbackroutingkey}")
    private String feedbackroutingkey;
    @Value("${dispatch.rabbitmq.tasksroutingkey}")
    private String tasksroutingkey;
    @Value("${dispatch.rabbitmq.priorityroutingkey}")
    private String priorityroutingkey;

    @Qualifier("dispatcherqueue")
    @Bean
    Queue dispatcherqueue() {
        return new Queue(dispatcherqueue, false);
    }

    @Qualifier("feedbackqueue")
    @Bean
    Queue feedbackqueue() {
        return new Queue(feedbackqueue, false);
    }

    @Qualifier("taskqueue")
    @Bean
    Queue tasksqueue() {
        return new Queue(tasksqueue, false);
    }
    @Qualifier("priorityqueue")
    @Bean
    Queue priorityqueue() {
        return new Queue(priorityqueue, false);
    }

    @Qualifier("dispatcherExchange")
    @Bean
    DirectExchange dispatcherexchange() {
        return new DirectExchange(dispatcherExchange);
    }

    @Qualifier("feedbackExchange")
    @Bean
    DirectExchange feedbackexchange() {
        return new DirectExchange(feedbackExchange);
    }

    @Qualifier("tasksExchange")
    @Bean
    DirectExchange tasksexchange() {
        return new DirectExchange(tasksExchange);
    }

    @Qualifier("priorityExchange")
    @Bean
    DirectExchange priorityExchange() {
        return new DirectExchange(priorityExchange);
    }

    @Bean
    Binding dispatchbinding(@Qualifier("dispatcherqueue") Queue dispatcherqueue, @Qualifier("dispatcherExchange") DirectExchange exchange) {
        return BindingBuilder.bind(dispatcherqueue).to(exchange).with(dispatcherroutingkey);
    }
    @Bean
    Binding feedbackbinding(@Qualifier("feedbackqueue") Queue feedbackqueue, @Qualifier("feedbackExchange")DirectExchange exchange) {
        return BindingBuilder.bind(feedbackqueue).to(exchange).with(feedbackroutingkey);
    }
    @Bean
    Binding tasksbinding(@Qualifier("tasksqueue") Queue tasksqueue, @Qualifier("tasksExchange")DirectExchange exchange) {
        return BindingBuilder.bind(tasksqueue).to(exchange).with(tasksroutingkey);
    }
    @Bean
    Binding prioritybinding(@Qualifier("priorityqueue") Queue priorityqueue, @Qualifier("priorityExchange")DirectExchange exchange) {
        return BindingBuilder.bind(priorityqueue).to(exchange).with(priorityroutingkey);
    }
    @Bean
    SimpleMessageListenerContainer dispatchcontainer(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter dispatchlistenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(dispatcherqueue);
        container.setMessageListener(dispatchlistenerAdapter);

        return container;
    }
    @Bean
    SimpleMessageListenerContainer feedbackcontainer(ConnectionFactory connectionFactory,
                                             MessageListenerAdapter feedbacklistenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(feedbackqueue);
        container.setMessageListener(feedbacklistenerAdapter);

        return container;
    }
    @Bean
    SimpleMessageListenerContainer taskscontainer(ConnectionFactory connectionFactory,
                                                     MessageListenerAdapter taskslistenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(tasksqueue);
        container.setMessageListener(taskslistenerAdapter);

        return container;
    }
    @Bean
    SimpleMessageListenerContainer priorityContainer(ConnectionFactory connectionFactory,
                                                     MessageListenerAdapter prioritylistenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(priorityqueue);
        prioritylistenerAdapter.setMessageConverter(new Jackson2JsonMessageConverter());
        container.setMessageListener(prioritylistenerAdapter);

        return container;
    }

    @Bean
    MessageListenerAdapter feedbacklistenerAdapter(RabbitMQReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveFeedback");
    }
    @Bean
    MessageListenerAdapter dispatchlistenerAdapter(RabbitMQReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveTask");
    }
    @Bean
    MessageListenerAdapter taskslistenerAdapter(RabbitMQReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveTaskFromEventQueue");
    }
    @Bean
    MessageListenerAdapter prioritylistenerAdapter(RabbitMQReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveTaskFromPriorityQueue");
    }
}
