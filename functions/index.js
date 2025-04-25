const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// Cloud Function que se dispara al subir una nueva foto
exports.notificarAdministradores = functions.firestore
    .document("fotos/{fotoId}")
    .onCreate(async (snap, context) => {
        const nuevaFoto = snap.data();

        const payload = {
            notification: {
                title: "📸 Nueva foto subida",
                body: "Un participante ha subido una foto pendiente de validación.",
            },
            data: {
                ruta: "validar_foto"
            }
        };

        try {
            const adminsSnapshot = await admin.firestore().collection("usuarios")
                .where("rol", "==", "administrador")
                .get();

            const tokens = [];
            adminsSnapshot.forEach(doc => {
                const token = doc.get("fcmToken");
                if (token) {
                    tokens.push(token);
                }
            });

            if (tokens.length === 0) {
                console.log("No se encontraron administradores con token.");
                return null;
            }

            const response = await admin.messaging().sendEachForMulticast({
                tokens: tokens,
                notification: payload.notification,
                data: payload.data
            });

            console.log("Notificaciones enviadas:", response);
            return null;
        } catch (error) {
            console.error("Error al enviar la notificación:", error);
            return null;
        }
    });
