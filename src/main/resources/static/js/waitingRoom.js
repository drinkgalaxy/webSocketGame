let stompClient = null;
let roomId = "abc123"; // 임시 room Id
let nickname = null;

function connectWebSocket() {
  // 1. URL 파라미터에서 roomId, nickname 추출
  const params = new URLSearchParams(window.location.search);
  roomId = params.get("roomId");
  nickname = params.get("nickname");

  if (!roomId || !nickname) {
    alert("잘못된 접근입니다.");
    window.location.href = `/start.html`;
    return;
  }

  const socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log("✅ WebSocket 연결됨");

    // 닉네임 서버로 전송
    stompClient.send("/pub/enter", {}, JSON.stringify({
      roomId: roomId,
      nickname: nickname
    }));

    // 1) 입장 알림 수신
    stompClient.subscribe("/sub/notice/" + roomId, (message) => {
      const notice = message.body;
      const chatBox = document.getElementById("chatBox");
      chatBox.innerHTML += `<p style="color:gray;"><em>${notice}</em></p>`;
    });

    // 2) 현재 인원 수 수신
    stompClient.subscribe("/sub/count/" + roomId, (message) => {
      const count = message.body;
      document.getElementById("playerCount").innerText = `현재 인원: ${count}명`;
    });

    // 3) 채팅 수신
    stompClient.subscribe("/sub/chat/" + roomId, (message) => {
      const msg = JSON.parse(message.body);
      const chatBox = document.getElementById("chatBox");
      chatBox.innerHTML += `<p><strong>${msg.sender}</strong>: ${msg.content}</p>`;
    });

    // 4) 참가자 목록 및 방장 정보 수신
    stompClient.subscribe(`/sub/room/members/${roomId}`, (message) => {
      const players = JSON.parse(message.body);
      const list = document.getElementById("playerList");
      list.innerHTML = "";
      players.forEach(p => {
        const li = document.createElement("li");
        li.textContent = p.nickname + (p.leader ? " 👑" : "");
        list.appendChild(li);
      });

      const leader = players.find(p => p.leader);
      document.getElementById("leaderName").textContent = leader ? leader.nickname : "없음";
    });
  });
}

// HTML 로드 시 자동 실행
window.addEventListener("load", connectWebSocket);
