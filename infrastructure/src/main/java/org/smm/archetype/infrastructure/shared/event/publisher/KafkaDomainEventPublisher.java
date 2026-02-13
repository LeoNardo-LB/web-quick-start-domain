// package org.smm.archetype.infrastructure.shared.event.publisher;
//
// import lombok.extern.slf4j.Slf4j;
// import org.smm.archetype.domain.shared.event.Event;
// import org.smm.archetype.infrastructure.shared.dal.generated.mapper.EventMapper;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.kafka.core.KafkaTemplate;
//
// /**
//  * Kafka 事件发布器
//  *
// 将事件发布到 Kafka 消息队列，支持异步处理。
//  *
// 工作流程：
//  * <ol>
//  *   <li>继承 DomainEventCollectPublisher，事件持久化到数据库（状态为 CREATED）</li>
//  *   <li>使用 IO 线程池异步发送到 Kafka 主题</li>
//  *   <li>发送成功后更新状态为 PUBLISHED</li>
//  *   <li>发送失败则保持 CREATED 状态，由定时任务重试</li>
//  * </ol>
//  *
// 适用于：
//  * <ul>
//  *   <li>分布式应用</li>
//  *   <li>生产环境</li>
//  *   <li>需要事件解耦的场景</li>
//  * </ul>
//
//
//  */
// @Slf4j
// public class KafkaDomainEventPublisher extends DomainEventCollectPublisher {
//
//     private final KafkaTemplate<String, Event<?>> kafkaTemplate;
//
//     @Value("${middleware.domain-event.consumer.kafka.topic}")
//     private String topic;
//
//     public KafkaDomainEventPublisher(KafkaTemplate<String, Event<?>> kafkaTemplate, EventMapper mapper) {
//         super(mapper);
//         this.kafkaTemplate = kafkaTemplate;
//     }
//
//     @Override
//     protected void doPublish(Event<?> event) {
//         kafkaTemplate.send(topic, event.getEid(), event);
//     }
//
// }
