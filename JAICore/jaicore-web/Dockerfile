FROM java:8
VOLUME /tmp

RUN apt-get update \
&& apt-get install -y clang \
&& apt-get install make

ENV CSVER=2.17.1
ENV CXX=clang++

RUN curl -OL https://github.com/stan-dev/cmdstan/releases/download/v$CSVER/cmdstan-$CSVER.tar.gz \
 && tar xzf cmdstan-$CSVER.tar.gz \
 && rm -rf cmdstan-$CSVER.tar.gz \
 && mv cmdstan-$CSVER cmdstan
 
ADD /stan/lc.stan stan/

RUN cd cmdstan \
&& make build ../stan/lc

ADD /build/libs/jaicore-web-0.0.1.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-jar","/app.jar"]