// server.js (Modified - NOT RECOMMENDED for production)
require('dotenv').config();
const express = require('express');
const admin = require('firebase-admin');
const jwt = require('jsonwebtoken');
const cors = require('cors');

const app = express();
app.use(express.json());

const corsOptions = {
    origin: 'http://localhost:3000',
};
app.use(cors(corsOptions));

const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });

app.post('/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const usersCollection = admin.firestore().collection('Users');
        const query = await usersCollection.where('email', '==', email).get();

        if (query.empty) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        let user;
        query.forEach((doc) => {
            user = doc.data();
        });

        // Plain text password comparison (NOT RECOMMENDED)
        if (user.password !== password) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        const role = user.role;
        const token = jwt.sign(
            { uid: query.docs[0].id, role },
            process.env.JWT_SECRET_KEY
        );

        res.json({ token, role });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

app.listen(3001, () => {
    console.log('Server started on port 3001');
});