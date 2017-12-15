# android-promise :

#### promiseObject.then().then().then().error();

A Javascript style Promise library for Android JVM. 

### How it works ?

```java
doSomeTask(int someValue, String extra)
    .then(res -> doSecondTask((MyObject) res))       // res is result form doSomeTask()
    .then(res -> doThirdTask((OtherObject) res)))    // res is result form doThirdTask()
    .then(res -> doFourthTask((int) res)))           // res is result form doThirdTask()
    .then(res-> doFivethTask())
    .then(res -> {
         // Consume result of the previous function
         return true;    // done
    })
    .error(err -> handleError());                    // Incase of any p.reject() call from above function error will be available here 
```

```java
public Promise doSomeTask(int someValue, String extra){
    Promise p = new Promise();
    new Thread(()->{
        // TODO : do some background operation.
        // When your work done, resolve the promise like below;
        // After resolve the library will pass the value to doSecondTack()
        // Suppose your resultant value is instance of MyObject, then
        // When it is error call reject() with some error message or an Exception
        // p.reject("Some Error happened")
        
        MyObject myObject = new MyObject();
        p.resolve(myObject);
    });
    return p;
}

public OtherObject doSecondTask(MyObject myInput) {
    // Do some syncronous or asyncronous task and return the result,
    // Still it work with your promise chain.
    // Folloing snipet is just a sample work flow
    
    OtherObject obj = new OtherObject();
    
    return obj;
}


public Promise doThirdTask(OtherObject otherObject){
    // Do some task, return value using promise
    Promise p = new Promise();
    
    // Your task
    
    p.resolve();
    
    return p;
}

public Promise doFourthTask(){
    return doSomePromisableTask();    // I am not writing defination of this fuction, let this function is very similar to 
                                      // `doSomeTask()` function
}

public Promise doFivethTask(){
    return doSomePromisableTask()
          .then(res -> {
              // Do some task here
              return 1;      // this one will be available in the next the or parent then which called this task
          });
}
```

### How to use in android?

*Download the source file add into your project src.*

__Promise.java__  Simply Copy this file into your project

__*It need JAVA 1.8 to compile__


## Description

The Promise object represents the eventual completion (or failure)
of an asynchronous operation, and its resulting value.
<p>
A Promise is a proxy for a value not necessarily known when
the promise is created. It allows you to associate handlers
with an asynchronous action's eventual success value or failure reason.
This lets asynchronous methods return values like synchronous methods:
instead of immediately returning the final value,
the asynchronous method returns a promise to supply the value
at some point in the future.
<p>
For more information on Javascript Promise
please visit the official Mozilla Promise documentation

@see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise">
 https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise</a>
<p>
This Android Promise Library slightly different than the native Javascript Promise.
This promise object has two imprtant method i.e. `resolve()` and `reject()`,
whenevey you done withe your process just call resolve or reject
function based on your state.
The resultant value will be automaticall passed as argument to the
followng `then()` or `error()` function.
<p>
You can write `n` numbers of `then()` chain.
<p>
It supports above JAVA 1.8
    
    
# LICENCE

/*
 * Copyright (c) 2017 CRAWLINK NETWORKS PVT. LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 

