# 구동방법


## IN DEV(IDE)

- IDE 설정에 아래 값 추가
  - `--spring.config.import=file:C:/workspace/frostbite/config/application-secret.properties`


## IN PROD
```shell

java -jar frostbite.jar \
--spring.profiles.active={profile} \
--spring.config.import=file:{secret-path}

```
