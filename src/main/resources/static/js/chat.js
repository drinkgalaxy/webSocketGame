function sendMessage() {
  const content = document.getElementById("msg").value;

  stompClient.send("/app/chat", {}, JSON.stringify({
    roomId: roomId,
    content: content
  }));
}
