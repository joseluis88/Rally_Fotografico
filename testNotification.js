const admin = require("firebase-admin");

// Asegúrate de que el archivo serviceAccountKey.json esté en la misma carpeta
const serviceAccount = require("./serviceAccountKey.json");

// Inicializamos Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

// El token FCM del móvil (usa uno válido de verdad aquí)
const token = "erMZ4wFeSKyApsTB-ndJ6F:APA91bGfKehkDaHV3Wh_0rPqHc0Y8dgUUGbQ1GydRmFtE0BlTzxfhAhLAUHi2TMS0sEcr1XvpntnsdtgURjnN7-iWu4C_u6b_-eOCJaGEhzeJFxDTimR3og";

const payload = {
  notification: {
    title: "📸 Foto subida",
    body: "Se ha subido una nueva foto para validar.",
  },
};

admin
  .messaging()
  .sendEachForMulticast({
    tokens: [token], // Siempre va como array
    notification: payload.notification,
  })
  .then((response) => {
    console.log("✅ Notificación enviada:", response);
  })
  .catch((error) => {
    console.error("❌ Error al enviar notificación:", error);
  });
