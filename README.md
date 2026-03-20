# AWS S3 Media Upload Demo

This project now implements the flow you described:

1. React uploads an image or video with student details to Spring Boot.
2. Spring Boot validates the request and media type.
3. Spring Boot uploads the file to AWS S3.
4. Spring Boot stores the S3 media URL and metadata in MySQL.
5. React fetches saved records from the backend and shows the media to the user.

## Project structure

- `src/main/java/...` - Spring Boot backend
- `frontend/` - separate React app

## Backend API

- `POST /api/students/upload`
  - multipart fields: `name`, `email`, `file`
- `GET /api/students`
  - returns all uploaded records with a `displayUrl` for preview
- `GET /api/students/{id}`
  - returns one record

## Backend configuration

Set these values before running the Spring Boot app.

```properties
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key
AWS_REGION=eu-north-1
AWS_BUCKET_NAME=your_bucket_name
AWS_PRESIGN_EXPIRY_MINUTES=15

DB_URL=jdbc:mysql://localhost:3306/aws_media?createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=root

SERVER_PORT=8081
APP_MEDIA_MAX_FILE_SIZE_BYTES=10485760
```

You can also rely on the default AWS credential chain instead of setting `AWS_ACCESS_KEY` and `AWS_SECRET_KEY`.

## How to run the backend

From the project root:

```powershell
.\mvnw spring-boot:run
```

The backend runs on `http://localhost:8081`.

## Frontend setup

The React app is in [`frontend`](P:\Boot\aws\frontend).

Create a frontend env file:

```powershell
cd frontend
Copy-Item .env.example .env
```

Default frontend env:

```env
VITE_API_BASE_URL=http://localhost:8081/api
```

## How to run the React app

From the project root:

```powershell
cd frontend
npm install
npm run dev
```

Vite will start the frontend on `http://localhost:5173`.

## Notes

- Allowed file types: `jpg`, `png`, `webp`, `gif`, `mp4`, `webm`, `mov`
- Max upload size default: `10 MB`
- For user preview, the backend returns a presigned S3 URL as `displayUrl`
- The database stores media metadata including `mediaUrl`, `s3Key`, `mediaType`, and `originalFileName`
