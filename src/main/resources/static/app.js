const featureInfo = {
  summarizer: ["Notes Summarizer", "Turn lengthy study material into clear, structured notes.", "Paste your notes here. The AI will identify the key ideas and summarize them."],
  quiz: ["Quiz Generator", "Create practice questions to test your understanding.", "Paste your study material. The AI will create five multiple-choice questions."],
  explainer: ["Concept Explainer", "Understand difficult topics through simple explanations and examples.", "Enter a topic or concept you want explained."],
  improver: ["Answer Improver", "Make your academic answers clearer, stronger, and better structured.", "Paste your draft answer here."],
  flashcards: ["Flashcard Generator", "Turn your notes into quick question-and-answer revision cards.", "Paste the material you want to convert into flashcards."],
  assistant: ["AI Study Assistant", "Ask questions and get clear, student-friendly explanations.", "Type your study question here."],
  rag: ["StudyMate RAG", "Get personalized answers based on your study material.", "Ask a question about your notes or study content."]
};
let selectedFeature = "summarizer";
let latestResult = "";
const input = document.getElementById("user-input");
const output = document.getElementById("output");
const generateButton = document.getElementById("generate-btn");
const copyButton = document.getElementById("copy-btn");
const ragUpload = document.getElementById("rag-upload");
const ragFile = document.getElementById("rag-file");
document.querySelectorAll(".nav-item").forEach(button => {
  button.addEventListener("click", () => {
    selectedFeature = button.dataset.feature;
    ragUpload.style.display = selectedFeature === "rag" ? "block" : "none";
    document.querySelectorAll(".nav-item").forEach(item => item.classList.toggle("active", item === button));
    document.getElementById("page-title").textContent = featureInfo[selectedFeature][0];
    document.getElementById("page-description").textContent = featureInfo[selectedFeature][1];
    input.placeholder = featureInfo[selectedFeature][2];
    latestResult = "";
    copyButton.disabled = true;
    output.innerHTML = '<div class="empty-state"><div class="empty-icon">✧</div><h3>Your result will appear here</h3><p>Enter your material and select Generate with AI.</p></div>';
  });
});
input.addEventListener("input", () => {
  document.getElementById("char-count").textContent = `${input.value.length} characters`;
});
document.getElementById("clear-btn").addEventListener("click", () => {
  input.value = "";
  document.getElementById("char-count").textContent = "0 characters";
  input.focus();
});
generateButton.addEventListener("click", async () => {
  if (selectedFeature === "rag") {
    const file = ragFile.files[0];

    if (!file) return showError("Please upload a document first.");

    const question = input.value.trim();

    if (!question) return showError("Please enter your question.");

    generateButton.disabled = true;
    output.textContent = "Processing your document...";

    try {
        const formData = new FormData();
        formData.append("file", file);

        const uploadResponse = await fetch("/api/documents/upload", {
            method: "POST",
            body: formData
        });

        const uploadData = await uploadResponse.json();

        if (!uploadResponse.ok) {
            throw new Error(uploadData.error || "Document upload failed.");
        }

        const askResponse = await fetch("/api/documents/ask", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ question })
        });

        const askData = await askResponse.json();

        if (!askResponse.ok) {
            throw new Error(askData.error || "Question could not be answered.");
        }

        latestResult = askData.answer || "";
        output.textContent = latestResult;
        copyButton.disabled = !latestResult;

    } catch (error) {
        showError(error.message);
    } finally {
        generateButton.disabled = false;
        generateButton.innerHTML = '<span>✦</span> Generate with AI <span class="arrow">→</span>';
    }

    return;
}
  const text = input.value.trim();
  if (!text) return showError("Please enter your notes, topic, answer, or question first.");
  if (text.length > 20000) return showError("Please keep your input below 20,000 characters.");
  generateButton.disabled = true;
  generateButton.textContent = "Generating response...";
  output.style.color = "";
  output.textContent = "StudyMate AI is processing your request. This may take a few seconds.";
  copyButton.disabled = true;
  try {
    const response = await fetch(`/api/ai/${selectedFeature}`, {
      method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify({text})
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "The request could not be completed.");
    latestResult = data.result || "";
    output.textContent = latestResult;
    copyButton.disabled = !latestResult;
  } catch (error) {
    showError(error.message || "Something went wrong. Please try again.");
  } finally {
    generateButton.disabled = false;
    generateButton.innerHTML = '<span>✧</span> Generate with AI <span class="arrow">→</span>';
  }
});
copyButton.addEventListener("click", async () => {
  if (!latestResult) return;
  try {
    await navigator.clipboard.writeText(latestResult);
    copyButton.textContent = "Copied!";
    setTimeout(() => copyButton.textContent = "Copy result", 1500);
  } catch {
    showError("Clipboard access was blocked by your browser.");
  }
});
function showError(message) {
  output.textContent = `Error: ${message}`;
  output.style.color = "#b42318";
}
