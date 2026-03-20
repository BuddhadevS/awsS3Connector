import { useEffect, useState } from 'react';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081/api';

const initialForm = {
  name: '',
  email: '',
  file: null
};

function App() {
  const [form, setForm] = useState(initialForm);
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    loadStudents();
  }, []);

  async function loadStudents() {
    setFetching(true);
    try {
      const response = await fetch(`${API_BASE_URL}/students`);
      if (!response.ok) {
        throw new Error('Failed to fetch uploaded media');
      }
      const data = await response.json();
      setStudents(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setFetching(false);
    }
  }

  function updateForm(event) {
    const { name, value, files } = event.target;
    setForm((current) => ({
      ...current,
      [name]: files ? files[0] : value
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess('');

    if (!form.name || !form.email || !form.file) {
      setError('Name, email, and a media file are required.');
      return;
    }

    const body = new FormData();
    body.append('name', form.name);
    body.append('email', form.email);
    body.append('file', form.file);

    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/students/upload`, {
        method: 'POST',
        body
      });

      const payload = await response.json();
      if (!response.ok) {
        throw new Error(payload.message || 'Upload failed');
      }

      setSuccess(payload.message);
      setForm(initialForm);
      const fileInput = document.getElementById('media-file-input');
      if (fileInput) {
        fileInput.value = '';
      }
      await loadStudents();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page-shell">
      <section className="hero">
        <div>
          <p className="eyebrow">React -> Spring Boot -> AWS S3</p>
          <h1>Upload student media and render it back from your backend.</h1>
          <p className="hero-copy">
            The browser sends the file to Spring Boot, Spring Boot validates and uploads it to S3,
            stores the media URL in MySQL, and this UI fetches the saved records for display.
          </p>
        </div>
        <div className="hero-stat">
          <span>{students.length}</span>
          <p>media records loaded</p>
        </div>
      </section>

      <section className="content-grid">
        <form className="upload-card" onSubmit={handleSubmit}>
          <h2>Upload media</h2>
          <label>
            Name
            <input name="name" value={form.name} onChange={updateForm} placeholder="Student name" />
          </label>
          <label>
            Email
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={updateForm}
              placeholder="student@example.com"
            />
          </label>
          <label>
            Image or video
            <input
              id="media-file-input"
              name="file"
              type="file"
              accept="image/*,video/mp4,video/webm,video/quicktime"
              onChange={updateForm}
            />
          </label>
          <button type="submit" disabled={loading}>
            {loading ? 'Uploading...' : 'Upload to S3'}
          </button>
          {error ? <p className="message error">{error}</p> : null}
          {success ? <p className="message success">{success}</p> : null}
        </form>

        <div className="gallery-card">
          <div className="gallery-head">
            <h2>Uploaded media</h2>
            <button type="button" onClick={loadStudents} disabled={fetching}>
              {fetching ? 'Refreshing...' : 'Refresh'}
            </button>
          </div>

          <div className="gallery-grid">
            {students.length === 0 && !fetching ? (
              <div className="empty-state">No media uploaded yet.</div>
            ) : null}

            {students.map((student) => (
              <article className="media-card" key={student.id}>
                <div className="media-frame">
                  {student.mediaType?.startsWith('video/') ? (
                    <video src={student.displayUrl} controls preload="metadata" />
                  ) : (
                    <img src={student.displayUrl} alt={student.name} />
                  )}
                </div>
                <div className="media-meta">
                  <h3>{student.name}</h3>
                  <p>{student.email}</p>
                  <p>{student.originalFileName}</p>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

export default App;
