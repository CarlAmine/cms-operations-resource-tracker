.PHONY: up down build seed logs restart clean

up:
	docker compose up -d

down:
	docker compose down

build:
	docker compose build --no-cache

logs:
	docker compose logs -f

restart:
	docker compose restart

clean:
	docker compose down -v --remove-orphans

seed:
	@echo "Running seed script..."
	docker compose exec backend java -jar app.jar --spring.profiles.active=seed || \
		(cd scripts && bash seed.sh)

backend-test:
	cd backend && ./mvnw test

frontend-react-dev:
	cd frontend-react && npm run dev

frontend-next-dev:
	cd frontend-next && npm run dev

setup:
	@echo "Copying environment files..."
	cp -n backend/.env.example backend/.env 2>/dev/null || true
	cp -n frontend-react/.env.example frontend-react/.env.local 2>/dev/null || true
	cp -n frontend-next/.env.example frontend-next/.env.local 2>/dev/null || true
	@echo "Done. Run 'make up' to start all services."
