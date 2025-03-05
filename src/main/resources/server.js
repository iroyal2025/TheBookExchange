const express = require('express');
const admin = require('firebase-admin');
const jwt = require('jsonwebtoken');
const cors = require('cors'); // Import cors

const app = express();
app.use(express.json());
app.use(cors()); // Use cors

// Initialize Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json'); // path to service account key
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });

app.post('/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const usersCollection = admin.firestore().collection('Users');
        const query = await usersCollection.where('email', '==', email).get();

        if (query.empty) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }

        let user;
        query.forEach((doc) => {
            user = doc.data();
        });

        // Password verification (replace with bcrypt)
        if (user.password !== password) {
            return res.status(401).json({ error: 'Invalid credentials' });
        }

        const role = user.role; // Assuming you have a 'role' field in your Users collection
        const token = jwt.sign({ uid: query.docs[0].id, role }, 'your-secret-key'); // query.docs[0].id to get the uid

        res.json({ token, role });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

app.listen(3001, () => {
    console.log('Server started on port 3001');
});