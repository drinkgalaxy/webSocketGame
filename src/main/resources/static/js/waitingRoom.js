let stompClient = null;
let roomId = "abc123"; // 임시 room Id
let nickname = null;

function connectWebSocket() {
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

    // 입장 메시지
    stompClient.send("/pub/enter", {}, JSON.stringify({
      roomId: roomId,
      nickname: nickname
    }));

    // 입장 알림
    stompClient.subscribe("/sub/notice/" + roomId, (message) => {
      const notice = message.body;
      const chatBox = document.getElementById("chatBox");
      chatBox.innerHTML += `<p style="color:gray;"><em>${notice}</em></p>`;
    });

    // 인원 수
    stompClient.subscribe("/sub/count/" + roomId, (message) => {
      const count = message.body;
      document.getElementById("playerCount").innerText = `현재 인원: ${count}명`;
    });

    // 채팅 수신
    stompClient.subscribe("/sub/chat/" + roomId, (message) => {
      const msg = JSON.parse(message.body);
      const chatBox = document.getElementById("chatBox");
      chatBox.innerHTML += `<p><strong>${msg.sender}</strong>: ${msg.content}</p>`;
    });

    // 👑 참가자 목록 + 방장 + 게임 제어
    stompClient.subscribe(`/sub/room/members/${roomId}`, (message) => {
      const players = JSON.parse(message.body);
      const list = document.getElementById("playerList");
      list.innerHTML = "";

      let leaderNickname = "없음";
      let isLeader = false;

      players.forEach(p => {
        const li = document.createElement("li");
        const isThisLeader = !!p.leader;

        if (isThisLeader) {
          leaderNickname = p.nickname;
          if (p.nickname === nickname) isLeader = true;
        }

        li.textContent = p.nickname + (isThisLeader ? " 👑" : "");
        list.appendChild(li);
      });

      document.getElementById("leaderName").innerText = leaderNickname;

      // 👑 방장만 라운드 선택 & 시작 버튼 표시
      const roundControl = document.getElementById("roundControl");
      const roundSelect = document.getElementById("roundSelect");
      const startBtn = document.getElementById("startBtn");

      if (isLeader) {
        roundControl.style.display = "block";

        // 옵션 초기화 및 갱신
        roundSelect.innerHTML = `<option value="">-- 선택하세요 --</option>`;
        [1, 2, 3].forEach(mult => {
          const value = players.length * mult;
          const option = document.createElement("option");
          option.value = value;
          option.textContent = `${value} 라운드`;
          roundSelect.appendChild(option);
        });

        // 선택 값 초기화
        roundSelect.value = "";
        startBtn.disabled = true;
      } else {
        roundControl.style.display = "none";
      }
    });

    // 게임 시작 알림 수신
    stompClient.subscribe("/sub/start/" + roomId, (message) => {
      alert(message.body);
      window.location.href = `/game.html?roomId=${roomId}&nickname=${nickname}`;
    });
  });
}

// 라운드 선택 시 버튼 활성화
function handleRoundSelection() {
  const selected = document.getElementById("roundSelect").value;
  document.getElementById("startBtn").disabled = selected === "";
}

// 게임 시작 메시지 전송
function startGame() {
  const rounds = document.getElementById("roundSelect").value;
  if (!rounds) return;

  stompClient.send("/pub/start", {}, JSON.stringify({
    roomId: roomId,
    rounds: parseInt(rounds)
  }));
}

window.addEventListener("load", connectWebSocket);

