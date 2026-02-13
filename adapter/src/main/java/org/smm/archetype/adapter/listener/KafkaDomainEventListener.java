// package org.smm.archetype.adapter.listener;
//
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.smm.archetype.adapter.event.EventDispatcher;
// import org.smm.archetype.domain.shared.event.Event;
// import org.springframework.kafka.annotation.KafkaListener;
//
// /**
//  * Kafka事件监听器，监听Kafka消息队列并委托给EventDispatcher处理。
//  */
// @Slf4j
// @RequiredArgsConstructor
// public class KafkaDomainEventListener {
//
//     private final EventDispatcher eventDispatcher;
//
// /**
//      * 处理Kafka消息。
//      * @param event 事件
//      */
//     @KafkaListener(topics = "${middleware.domain-event.consumer.kafka.topic}")
//     public void onEvent(Event<?> event) {
//         log.debug("Received event from Kafka: eventId={}, type={}",
//                 event.getEid(), event.getClass().getSimpleName());
//
//         // 委托给 EventDispatcher 处理（首次消费，isRetry=false）
//         eventDispatcher.dispatch(event, false);
//     }
//
// }
