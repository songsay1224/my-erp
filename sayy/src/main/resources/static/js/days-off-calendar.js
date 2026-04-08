(() => {
  function pad2(n) {
    return String(n).padStart(2, "0");
  }

  // Local date -> YYYY-MM-DD (timezone safe for "date-only" semantics)
  function toYmd(date) {
    return `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}`;
  }

  function getCsrf() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
    return { token, header };
  }

  function jsonFetch(url, options = {}) {
    const { token, header } = getCsrf();
    const headers = new Headers(options.headers || {});
    headers.set("Accept", "application/json");

    if (options.body && !headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json");
    }
    if (token && header) {
      headers.set(header, token);
    }

    return fetch(url, { ...options, headers }).then(async (res) => {
      if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText} ${text}`.trim());
      }
      const ct = res.headers.get("content-type") || "";
      if (ct.includes("application/json")) return res.json();
      return res.text();
    });
  }

  function init() {
    const calendarEl = document.getElementById("daysOffCalendar");
    if (!calendarEl || !window.FullCalendar) return;

    const calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: "dayGridMonth",
      locale: "ko",
      height: "auto",
      nowIndicator: true,
      selectable: false,
      editable: false,
      headerToolbar: {
        left: "prev,next today",
        center: "title",
        right: "dayGridMonth,dayGridWeek",
      },
      events: (fetchInfo, successCallback, failureCallback) => {
        const start = toYmd(fetchInfo.start);
        const end = toYmd(fetchInfo.end);
        jsonFetch(`/admin/api/days-off/events?start=${start}&end=${end}`)
          .then(successCallback)
          .catch(failureCallback);
      },
    });

    calendar.render();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();

