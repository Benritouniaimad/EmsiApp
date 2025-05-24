const express = require('express');
const axios = require('axios');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

app.post('/ask', async (req, res) => {
  const { question } = req.body;

  try {
    const response = await axios.post('https://openrouter.ai/api/v1/chat/completions', {
      model: "openai/gpt-3.5-turbo",
      messages: [{ role: "user", content: question }]
    }, {
      headers: {
        "Authorization": `Bearer ${process.env.OPENROUTER_API_KEY}`,
        "Content-Type": "application/json"
      }
    });

    res.json({ answer: response.data.choices[0].message.content });
  } catch (err) {
    console.error(err.response?.data || err.message);
    res.status(500).json({ error: "Erreur assistant virtuel" });
  }
});

const PORT = 3002;
app.listen(PORT, () => console.log(`✅ Backend running on http://localhost:${PORT}`));
