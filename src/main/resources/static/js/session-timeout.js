const INACTIVITY_TIMEOUT = 10 * 60 * 60 * 1000; // 10 часов

const STORAGE_KEY = 'lastUserActivity';

function updateActivity() {
    localStorage.setItem(STORAGE_KEY, Date.now().toString());
}

// Если времени ещё нет — пользователь только вошёл/открыл приложение
if (!localStorage.getItem(STORAGE_KEY)) {
    updateActivity();
}

// Реальная активность пользователя
[
    'mousedown',
    'keydown',
    'touchstart',
    'scroll'
].forEach(eventName => {
    document.addEventListener(eventName, updateActivity);
});

// Проверяем раз в минуту
setInterval(() => {

    const lastActivity = Number(
        localStorage.getItem(STORAGE_KEY)
    );

    const inactiveTime =
        Date.now() - lastActivity;

    if (inactiveTime >= INACTIVITY_TIMEOUT) {

        localStorage.removeItem(STORAGE_KEY);

        window.location.href = '/logout';
    }

}, 60_000);
