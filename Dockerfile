# STAGE 1: Build ứng dụng sử dụng Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy file pom.xml và tải trước các thư viện (để tận dụng cache của Docker)
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ source code và tiến hành build file JAR (Bỏ qua chạy test để build nhanh hơn)
COPY backend/src ./src
RUN mvn clean package -DskipTests

# STAGE 2: Chạy ứng dụng với môi trường JRE nhỏ gọn
# STAGE 2: Chạy ứng dụng với môi trường JRE nhỏ gọn (Hỗ trợ ARM64 cho Mac M-series)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy file JAR từ Stage 1 sang Stage 2
COPY --from=build /app/target/FridgeAI-0.0.1-SNAPSHOT.jar app.jar

# Tạo thư mục để lưu trữ ảnh hóa đơn bên trong container
RUN mkdir -p uploads/receipts

# Expose port 8080 và chạy ứng dụng
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]