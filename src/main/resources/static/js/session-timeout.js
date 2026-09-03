const INACTIVITY_TIMEOUT = 10 * 60 * 60 * 1000; // 10 часов
const SESSION_CHECK_INTERVAL = 10 * 1000; // 10 секунд
const ACTIVE_USER_WINDOW = 2 * 60 * 1000; // Проверяем только недавно активного пользователя

const STORAGE_KEY = 'lastUserActivity';
let sessionCheckInProgress = false;

function updateActivity() {
    localStorage.setItem(STORAGE_KEY, Date.now().toString());
}

function redirectToLogin() {
    localStorage.removeItem(STORAGE_KEY);
    window.location.replace('/login?expired');
}

async function checkSession() {
    if (sessionCheckInProgress) {
        return;
    }

    sessionCheckInProgress = true;

    try {
        const response = await fetch('/session/heartbeat', {
            method: 'GET',
            credentials: 'same-origin',
            cache: 'no-store',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        });

        if (response.status === 401
            || response.status === 403
            || response.redirected
            || response.url.includes('/login')) {
            redirectToLogin();
        }
    } catch (error) {
        // Временная потеря сети не должна принудительно завершать сессию.
        console.warn('Не удалось проверить состояние сессии', error);
    } finally {
        sessionCheckInProgress = false;
    }
}

// Загрузка защищённой страницы сама является активностью пользователя.
updateActivity();

// Реальная активность пользователя
[
    'mousedown',
    'keydown',
    'touchstart',
    'scroll'
].forEach(eventName => {
    document.addEventListener(eventName, updateActivity);
});

// Проверяем локальный таймаут и наличие серверной сессии каждые 10 секунд.
setInterval(() => {
    const lastActivity = Number(localStorage.getItem(STORAGE_KEY));
    const inactiveTime = Date.now() - lastActivity;

    if (!lastActivity || inactiveTime >= INACTIVITY_TIMEOUT) {
        redirectToLogin();
        return;
    }

    if (document.visibilityState === 'visible' && inactiveTime <= ACTIVE_USER_WINDOW) {
        checkSession();
    }
}, SESSION_CHECK_INTERVAL);

// Сразу обнаруживаем потерянную сессию при возврате на вкладку.
document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
        checkSession();
    }
});
