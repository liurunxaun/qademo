# 运行方式

先运行python

后端：

``cd /Users/liurunxuan/学习/科大讯飞实习/ResearchAssistant/demo/backend``

``mvn clean install``

``mvn spring-boot:run``

前端：

``cd /Users/liurunxuan/学习/科大讯飞实习/ResearchAssistant/demo/frontend``

``pnpm install``

``pnpm run serve``

先运行后端，再运行前端。使得后端在8080，监听8081.前端在8081，向8080发请求。

问题示例：

Doctor, I have been experiencing a hoarse voice for a few weeks now and it's not getting any better despite taking medication. What could be the problem?"