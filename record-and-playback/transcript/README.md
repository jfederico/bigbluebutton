
# 📄 Transcript Format for BigBlueButton Recordings

The **transcript** format is an extension of BigBlueButton’s recording formats that automatically generates a text transcript for audio using Whisper AI and publishes it alongside the recording.

This document describes the installation requirements and setup steps required to enable the `transcript` format on a BigBlueButton 3.0 server.

---

## 📌 Requirements

- BigBlueButton 3.0 installed and running
- Python 3 and `pip` installed (already included with BBB 3.0)
- `whisper` Python module (used to generate audio transcripts)

---

## 📦 Installing Whisper

To enable the `transcript` format, you need to install the Whisper command-line tool on the server.

### 1️⃣ Upgrade `pip` (recommended)

```bash
pip install --upgrade pip
```

### 2️⃣ Install `whisper`

```bash
pip install -U openai-whisper
```

This will:

- Install the Whisper Python package
- Install its dependencies (including PyTorch)

> **Note:** If PyTorch fails to install automatically, refer to [PyTorch installation guide](https://pytorch.org/get-started/locally/) to install the appropriate version for your server.

---

## 🧹 (Optional) Pre-download Whisper model

By default, Whisper will automatically download the `base` model the first time it is used.

If you want to pre-download the model to avoid delays or network dependency during transcription:

```bash
mkdir -p /var/bigbluebutton/whisper-models
cd /var/bigbluebutton/whisper-models
wget https://huggingface.co/openai/whisper-base/resolve/main/base.pt
```

This directory is automatically used by the transcript processing scripts.

---

## 🔧 Configuring BigBlueButton to support transcript playback

In addition to the Whisper installation, the following configuration steps are required to enable transcript recordings:

### 1️⃣ Install `transcript.nginx`

Place the `transcript.nginx` file into:

```
/usr/share/bigbluebutton/nginx/
```

Then restart nginx to apply the configuration:

```bash
sudo systemctl restart nginx
```

This makes the transcript recordings available over HTTP/S.

### 2️⃣ Enable `transcript` format in recording.yml

Edit the following file:

```
/etc/bigbluebutton/recording/recording.yml
```

Add `transcript` to the list of published formats. Example:

```yaml
published:
  - presentation
  - transcript
```

This activates the transcript format as part of BigBlueButton’s recording processing pipeline.

### 3. Install the Ruby scripts

The processing and publishing Ruby scripts must be placed in the following locations so they are automatically picked up by the BigBlueButton recording manager:

```
/usr/local/bigbluebutton/core/scripts/process/transcript.rb
/usr/local/bigbluebutton/core/scripts/publish/transcript.rb
```

Ensure that the scripts are executable and owned by the bigbluebutton user.

### 4. Restart the BigBlueButton recording manager

After copying the scripts, restart the recording processing and publishing services so they load the new format:

```
sudo systemctl restart bbb-rap-process
sudo systemctl restart bbb-rap-publish
```

Refer to the BigBlueButton documentation on custom recording formats for more details.


---

## ✅ Transcript Processing Flow

Once installed and configured:

- The process script (`process/transcript.rb`) will:
  - Convert audio.ogg to audio.mp3
  - Generate `recording.txt` transcript using Whisper
  - Create metadata.xml

- The publish script (`publish/transcript.rb`) will:
  - Copy the mp3 and transcript text file
  - Update metadata.xml to expose the transcript in the playback API
  - Finalize and publish the transcript format for access via URL

---

## 📎 Final Notes

- The Whisper model will be cached after the first use in `/var/bigbluebutton/whisper-models`.
- The transcript text file (`recording.txt`) will be available in the published transcript format path and linked via the recordings API.

---

## 📞 Support

For further information, consult the BigBlueButton developer documentation or contact your system administrator.
