const API_URL = "http://localhost:8082/meet-tracking/events";
console.log("CLASSFLOW background script loaded");
async function sendTrackingEvent(meetingCode, eventType) {
    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                meetingCode: meetingCode,
                eventType: eventType,
                source: "EDGE_EXTENSION"
            })
        });
		if (!response.ok) {
		    const errorText = await response.text();
		    console.error(
		        "CLASSFLOW tracking failed:",
		        response.status,
		        errorText
		    );
		    return;
		}

        const result = await response.json();
        console.log("CLASSFLOW event saved:", result);

    } catch (error) {
        console.error("Cannot connect to CLASSFLOW backend:", error);
    }
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === "MEET_TRACKING_EVENT") {
        sendTrackingEvent(message.meetingCode, message.eventType)
            .then(() => sendResponse({ received: true }))
            .catch(error => {
                console.error("Tracking error:", error);
                sendResponse({ received: false });
            });

        return true;
    }
});
