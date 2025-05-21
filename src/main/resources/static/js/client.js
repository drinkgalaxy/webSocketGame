let stompClient = null;
let roomId = null;
let playerMark = null;
let nickname = null;

function joinRoom() {
    if (stompClient) {
        leaveRoom();
        return;
    }

    roomId = document.getElementById("roomIdInput").value.trim();
    nickname = document.getElementById("nicknameInput").value.trim();

    if (!roomId || !nickname) return alert("Room ID와 닉네임을 모두 입력하세요.");

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        stompClient.subscribe(`/topic/board/${roomId}`, (message) => {
            const boardData = JSON.parse(message.body);
            renderBoard(boardData);
        });

        // 에러 메시지 구독
        const sessionId = socket._transport.url.split('/')[5]; // 현재 접속자의 세션 ID
        stompClient.subscribe(`/topic/errors/${sessionId}`, (message) => {
            alert(message.body);
        });

        stompClient.send("/app/init", {}, JSON.stringify({ roomId, nickname }));

        document.getElementById("entryButton").innerText = "퇴장";
    });
}

function sendMove(position) {
    if (!stompClient || !roomId) return;

    stompClient.send("/app/move", {}, JSON.stringify({
        roomId: roomId,
        position: position
    }));
}

function renderBoard(boardData) {
    const { board, turn, result, yourMark } = boardData;

    if (yourMark && !playerMark) {
        playerMark = yourMark;
    }

    const boardDiv = document.getElementById("board");
    boardDiv.innerHTML = "";

    board.forEach((cell, idx) => {
        const btn = document.createElement("button");
        btn.innerText = cell || " ";
        btn.onclick = () => sendMove(idx);
        btn.disabled = !!cell || result != null || turn !== playerMark;
        boardDiv.appendChild(btn);
    });

    const info = document.getElementById("info");
    const restartBtn = document.getElementById("restartButton");

    if (result) {
        info.innerText = result === "DRAW" ? "무승부!" : `${result}`;
        restartBtn.style.display = "inline-block";
    } else {
        restartBtn.style.display = "none";
        info.innerText = (turn === playerMark) ? "당신의 차례입니다." : "상대 차례입니다.";
    }
}

function restartGame() {
    if (!stompClient || !roomId) return;
    stompClient.send("/app/restart", {}, JSON.stringify({ roomId: roomId }));
}

function leaveRoom() {
    if (stompClient) {
        stompClient.disconnect(() => {
            console.log("Disconnected");
            stompClient = null;
            playerMark = null;
            document.getElementById("info").innerText = "게임을 종료했습니다.";
            document.getElementById("board").innerHTML = "";
            document.getElementById("entryButton").innerText = "입장";
        });
    }
}

