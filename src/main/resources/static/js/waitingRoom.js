let stompClient = null;
let roomId = "abc123"; // 임시 URL 코드
let nickname = null;

function connectWebSocket(userNickname) {
  nickname = userNickname;

  const socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log("WebSocket 연결됨");

    // 1) 서버에 닉네임 전송
    stompClient.send("/app/enter", {}, JSON.stringify({
      roomId: roomId,
      nickname: nickname
    }));

    // 2) 입장 알림 수신
    stompClient.subscribe("/topic/notice/" + roomId, (message) => {
      const notice = message.body;
      const chatBox = document.getElementById("chatBox");
      chatBox.innerHTML += `<p style="color:gray;"><em>${notice}</em></p>`;
    });

    // 3) 인원 수 수신
    stompClient.subscribe("/topic/count/" + roomId, (message) => {
      const count = message.body;
      document.getElementById("playerCount").innerText = `현재 인원: ${count}명`;
    });

    // 4) 채팅 수신
    stompClient.subscribe("/topic/chat/" + roomId, (message) => {
      const msg = JSON.parse(message.body);
      const chatBox = document.getElementById("chatBox");
      chatBox.innerHTML += `<p><strong>${msg.sender}</strong>: ${msg.content}</p>`;
    });
  });
}
