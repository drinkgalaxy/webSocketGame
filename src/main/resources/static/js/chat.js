function sendMessage() {
  const content = document.getElementById("msg").value;

  stompClient.send("/pub/chat", {}, JSON.stringify({
    roomId: roomId,
    content: content
  }));
}
