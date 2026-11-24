# Sillage

A fragrance discovery MVP with a Java 21 / Spring Boot API and a React / TypeScript frontend. The default catalog comes from the 10ML Fragrance Index.

## Run locally

Start the API:

```powershell
Set-Location [path]
mvn spring-boot:run
```

In another terminal, start the frontend:

```powershell
Set-Location [path]
npm install
npm run dev
```

Open `http://localhost:5173`.

## Data providers

The frontend only talks to the local REST API, while the backend reads through `FragranceProvider`. By default, `TenMlFragranceProvider` fetches up to 20 relevant records per search from the 10ML Fragrance Index and retrieves detailed note pyramids for fragrance pages.

10ML requires visible linked attribution wherever its data appears. The UI includes the required credit:

`Data: 10ML Fragrance Index` → `https://10ml.com/perfume-api`

No API key is required. To use the original local sample catalog instead, start the backend with:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--fragrance.provider=demo"
```

The application does not scrape Fragrantica.

## Images

Fragrance and note imagery is searched through Wikimedia Commons. Images are only displayed when the search result passes a strict relevance check and includes its source and license metadata. Every displayed Commons image includes author and license links; generated bottle and note visuals are used when no suitable licensed image is available.

Exact product photos can optionally come from the Perfume Click Awin feed. This integration is inactive until the publisher account has merchant approval and both variables are configured:

```powershell
$env:AWIN_PUBLISHER_ID = 'your-publisher-id'
$env:AWIN_API_TOKEN = 'your-secret-token'
```

Optional overrides are `AWIN_ADVERTISER_ID` (defaults to `6561`) and `AWIN_LOCALE` (defaults to `en_GB`). Awin images are used first; Commons and generated illustrations remain as fallbacks.

## Password reset email

Forgot Password uses a six-digit code that expires after 15 minutes. Configure an SMTP account through environment variables before starting the backend:

```powershell
$env:MAIL_HOST = 'smtp.example.com'
$env:MAIL_PORT = '587'
$env:MAIL_USERNAME = 'smtp-username'
$env:MAIL_PASSWORD = 'smtp-password'
$env:MAIL_FROM = 'no-reply@sillage.app'
```

`MAIL_SMTP_AUTH` and `MAIL_STARTTLS` default to `true`. SMTP credentials are read only from the environment and must not be committed.

## API

- `GET /api/fragrances`
- `GET /api/fragrances/{id}`
- `GET /api/fragrances/facets`

Search parameters: `query`, `manufacturer`, `note`, `fromYear`, and `toYear`.
