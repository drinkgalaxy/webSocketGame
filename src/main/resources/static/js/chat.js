function sendMessage() {
  const content = document.getElementById("msg").value;

  if (stompClient && stompClient.connected) {
    stompClient.send("/pub/chat", {}, JSON.stringify({
      roomId: roomId,
      content: content
    }));
  }
}
