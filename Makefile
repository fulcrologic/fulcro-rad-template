cljs:
	pnpm exec shadow-cljs server

report:
	pnpm exec shadow-cljs run shadow.cljs.build-report main report.html

release:
	TIMBRE_LEVEL=:warn pnpm exec shadow-cljs release main

server:
	clj -A:dev -M -m com.example.components.server