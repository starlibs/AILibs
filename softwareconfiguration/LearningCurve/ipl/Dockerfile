FROM python:3.6.5-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --trusted-host pypi.python.org -r requirements.txt
EXPOSE 8081
ADD . /app
CMD ["python", "app.py"]
