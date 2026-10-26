
(() => {
    if (window.__classflowTrackerLoaded) {
        return;
    }

    window.__classflowTrackerLoaded = true;

    const pathParts = location.pathname.split("/").filter(Boolean);
const meetingCode = pathParts[pathParts.length - 1];

console.log("CLASSFLOW meeting code:", meetingCode);

    if (!meetingCode) {
        return;
    }

    let lastState = null;
    let lastSentAt = 0;

    function detectMeetState() {
        const bodyText = document.body?.innerText || "";

        const ended =
            /you left the meeting|meeting has ended|return to home screen/i
                .test(bodyText);

        const joined =
            /leave call|leave meeting/i.test(bodyText);

        if (ended) {
            return "MEETING_LEFT";
        }

        if (joined) {
            return "MEETING_JOINED";
        }

        return null;
    }

    function checkState() {
        const state = detectMeetState();

        if (!state || state === lastState) {
            return;
        }

        const now = Date.now();

        if (now - lastSentAt < 3000) {
            return;
        }

        lastState = state;
        lastSentAt = now;

        chrome.runtime.sendMessage({
            type: "MEET_TRACKING_EVENT",
            meetingCode: meetingCode,
            eventType: state
        });
    }

    checkState();

    const observer = new MutationObserver(() => {
        checkState();
    });

    observer.observe(document.body, {
        childList: true,
        subtree: true,
        characterData: true
    });

    setInterval(checkState, 5000);
})();
