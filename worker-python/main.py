"""DevPulse Worker – Health-Check Engine.

Fetches monitored services from the Java backend, pings each URL and
reports the updated status (UP/DOWN) back via PATCH. On DOWN it also
creates an incident.
"""

import os

import httpx
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

BACKEND_BASE = os.environ.get("BACKEND_BASE", os.environ.get("BACKEND_URL", "http://localhost:8080"))
TIMEOUT = float(os.environ.get("CHECK_TIMEOUT", "5"))
REQUEST_TIMEOUT = float(os.environ.get("REQUEST_TIMEOUT", "10"))

app = FastAPI(title="DevPulse Worker", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class CheckResult(BaseModel):
    serviceId: int
    url: str
    status: str
    error: str | None = None


def _fetch_services() -> list[dict]:
    """Fetch all monitored services from the Java backend."""
    try:
        resp = httpx.get(f"{BACKEND_BASE}/api/services", timeout=REQUEST_TIMEOUT)
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY,
                            detail=f"Backend unreachable: {exc}") from exc
    if resp.status_code >= 400:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY,
                            detail=f"Backend returned {resp.status_code}")
    return resp.json()


def _check_url(url: str) -> tuple[str, str | None]:
    """Ping a single URL. Returns (status, error_message)."""
    try:
        resp = httpx.get(url, timeout=TIMEOUT, follow_redirects=True)
        return ("UP" if resp.status_code < 500 else "DOWN", None)
    except httpx.HTTPError as exc:
        return ("DOWN", str(exc))


def _patch_service(service_id: int, new_status: str) -> None:
    try:
        resp = httpx.patch(
            f"{BACKEND_BASE}/api/services/{service_id}",
            json={"status": new_status},
            timeout=REQUEST_TIMEOUT,
        )
    except httpx.HTTPError as exc:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY,
                            detail=f"PATCH failed for service {service_id}: {exc}") from exc
    if resp.status_code >= 400:
        raise HTTPException(status_code=status.HTTP_502_BAD_GATEWAY,
                            detail=f"PATCH failed for service {service_id}")


def _create_incident(service_id: int, error: str | None) -> None:
    try:
        httpx.post(
            f"{BACKEND_BASE}/api/incidents",
            json={
                "serviceId": service_id,
                "title": f"Service {service_id} unreachable",
                "severity": "HIGH",
            },
            timeout=REQUEST_TIMEOUT,
        )
    except httpx.HTTPError:
        # Incident creation is best-effort; never break the check cycle.
        pass


@app.get("/health")
async def health() -> dict:
    return {"status": "ok", "backend": BACKEND_BASE}


@app.post("/check-all")
async def check_all() -> list[CheckResult]:
    services = _fetch_services()
    results: list[CheckResult] = []
    for svc in services:
        svc_id = svc["id"]
        url = svc["url"]
        new_status, error = _check_url(url)
        _patch_service(svc_id, new_status)
        if new_status == "DOWN":
            _create_incident(svc_id, error)
        results.append(CheckResult(serviceId=svc_id, url=url, status=new_status, error=error))
    return results


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)