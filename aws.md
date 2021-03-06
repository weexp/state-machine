How could CQRS be used on AWS?
---------------------------------
DRAFT, just playing with ideas at the moment

Specifically how can scalable processing of the Command part of CQRS be achieved?

## Components

* SQS - *signalQueue*
* SNS - *signalTopic*
* Lambda - *processorLambda*
* DynamoDB - Entity and EntityEvent tables, Entity has nextId attribute that is incremented atomically, Entity also has wip attribute that is changed atomically  
* API Gateway - *signalResource*
* *signalResource* puts a text signal on *signalQueue* and then sends RUN to *signalTopic*
* *processorLambda* is subscribed to *signalTopic*

## processorLambda

processorLambda does the following:

```
get first signal from signalQueue
if no message then exit

while (true) {
  wip = wip attribute for signal.cls and signal.id from DynamoDb.Entity table
  time = wip.get 
  now = currentTimeEpochMs
  if (time == 0) {
    if (wip.compareAndSet(0, now)) {
      updateStuff()
      break;
    }
  } else if (now - time > TIMEOUT_MS) {
    if (wip.compareAndSet(time, now)) {
      updateStuff()
      break;  
    }
  }
}

void updateStuff() {
  //update stuff   
  processor = create Processor
  read events from DynamoDb.EntityEvent table in order
    replay event through processor
  push signal.event through processor
  for each s in processor.signalsToOthers
    call signalResource with s
  insert signal.event into DynamoDb.EntityEvent table 
  if (!wip.compareAndSet(time, 0)) {
      send RUN to signalTopic
    }
  } 
}
```

