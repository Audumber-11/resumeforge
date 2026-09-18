// ===== ResumeForge Builder - Complete JavaScript =====

// ===== Utilities =====
function val(id) { return document.getElementById(id)?.value || ''; }
function esc(s) { return (s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }

function showSaveToast(msg) {
    const t = document.createElement('div');
    t.className = 'alert alert-success';
    t.style.cssText = 'position:fixed;top:80px;right:20px;z-index:9999;animation:fadeIn 0.3s ease;';
    t.innerHTML = '&#10003; ' + msg;
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 3000);
}

// ===== Section Navigation =====
function showSection(name, el) {
    document.querySelectorAll('.section-form').forEach(s => s.classList.remove('active'));
    var target = document.getElementById('section-' + name);
    if (target) target.classList.add('active');
    document.querySelectorAll('.sidebar-nav a').forEach(a => a.classList.remove('active'));
    if (el) el.classList.add('active');
    if (event) event.preventDefault();
    return false;
}

function getSectionOrder() {
    return Array.from(document.querySelectorAll('#section-order .card')).map(el => el.dataset.section).join(',');
}

// ===== Read all resume data from DOM =====
function readCards(selector, fieldNames) {
    const items = [];
    document.querySelectorAll(selector + ' .card').forEach(card => {
        const item = {};
        fieldNames.forEach(f => {
            const el = card.querySelector('[data-field="' + f + '"]');
            item[f] = el ? (el.value || '') : '';
            if (f === 'currentlyWorking') item[f] = (el && (el.value === 'true' || el.checked));
        });
        items.push(item);
    });
    return items;
}

function readResumeData() {
    return {
        firstName: val('firstName'),
        lastName: val('lastName'),
        professionalTitle: val('professionalTitle'),
        personalEmail: val('personalEmail'),
        phone: val('phone'),
        location: val('location'),
        website: val('website'),
        linkedin: val('linkedin'),
        github: val('github'),
        summary: val('summary'),
        education: readCards('#education-list', ['institution','degree','field','startDate','endDate','grade','description']),
        experience: readCards('#experience-list', ['jobTitle','company','location','employmentType','startDate','endDate','currentlyWorking','description','achievements']),
        skills: readCards('#skills-list', ['name','category','proficiency']),
        projects: readCards('#projects-list', ['name','description','technologies','role','startDate','endDate','keyContributions','githubUrl','liveDemoUrl']),
        certifications: readCards('#certifications-list', ['name','issuingOrganization','date','credentialId','credentialUrl']),
        achievements: readCards('#achievements-list', ['title','description','date']),
        languages: readCards('#languages-list', ['name','proficiency'])
    };
}

// ===== Save =====
function saveAll() {
    const data = {
        firstName: val('firstName'), lastName: val('lastName'),
        professionalTitle: val('professionalTitle'), personalEmail: val('personalEmail'),
        phone: val('phone'), location: val('location'), website: val('website'),
        linkedin: val('linkedin'), github: val('github'), summary: val('summary'),
        templateId: templateId, primaryColor: primaryColor,
        fontFamily: fontFamily, fontSize: fontSize,
        sectionOrder: getSectionOrder()
    };
    fetch('/builder/' + resumeId + '/save', {
        method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data)
    }).then(r => r.json()).then(d => {
        if (d.success) showSaveToast('Resume saved!');
        else alert('Error: ' + (d.error || 'Unknown'));
    }).catch(() => showSaveToast('Save failed'));
}

// ===== Preview - renders ALL sections =====
function updatePreview() {
    const d = readResumeData();
    const c = primaryColor;
    const ff = fontFamily;
    const fs = fontSize;
    let html = '';

    // Check if a template renderer exists
    if (typeof templates !== 'undefined' && templates[templateId]) {
        html = templates[templateId](d, c, ff, fs);
    } else {
        // Fallback to old rendering for legacy templates
        const sections = getSectionOrder().split(',').map(s => s.trim());
        html = _renderLegacyPreview(d, sections, c, ff, fs);
    }

    document.getElementById('resumePreview').innerHTML = html;
}

function _renderLegacyPreview(d, sections, c, ff, fs) {
    let html = '';
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    html += '<div class="preview-header" style="border-bottom-color:' + c + ';">';
    html += '<h1 style="color:' + c + ';font-family:' + ff + ';font-size:' + (fs + 9) + 'pt;">' + esc(name) + '</h1>';
    if (d.professionalTitle) html += '<div class="title" style="font-family:' + ff + ';">' + esc(d.professionalTitle) + '</div>';
    html += '<div class="contact">';
    if (d.personalEmail) html += '<span>' + esc(d.personalEmail) + '</span>';
    if (d.phone) html += '<span>' + esc(d.phone) + '</span>';
    if (d.location) html += '<span>' + esc(d.location) + '</span>';
    html += '</div>';
    if (d.linkedin || d.github || d.website) {
        html += '<div style="font-size:8.5pt;color:#666;margin-top:4px;">';
        if (d.linkedin) html += '<a href="' + esc(d.linkedin) + '" style="color:' + c + ';text-decoration:none;margin-right:10px;">LinkedIn</a>';
        if (d.github) html += '<a href="' + esc(d.github) + '" style="color:' + c + ';text-decoration:none;margin-right:10px;">GitHub</a>';
        if (d.website) html += '<a href="' + esc(d.website) + '" style="color:' + c + ';text-decoration:none;">Portfolio</a>';
        html += '</div>';
    }
    html += '</div>';

    // Render each section in order
    for (const s of sections) {
        // Summary
        if (s === 'summary' && d.summary) {
            html += '<div class="preview-section"><div class="preview-section-title" style="color:' + c + ';">Professional Summary</div>';
            html += '<div style="font-size:' + (fontSize - 0.5) + 'pt;color:#333;">' + esc(d.summary) + '</div></div>';
        }
        // Education
        if (s === 'education' && d.education.length > 0) {
            html += '<div class="preview-section"><div class="preview-section-title" style="color:' + c + ';">Education</div>';
            d.education.forEach(e => {
                html += '<div class="preview-entry">';
                html += '<div class="preview-entry-header"><span class="preview-entry-title">' + esc(e.degree) + (e.field ? ' in ' + esc(e.field) : '') + '</span>';
                html += '<span class="preview-entry-date">' + esc(e.startDate) + (e.endDate ? ' - ' + esc(e.endDate) : '') + '</span></div>';
                html += '<div class="preview-entry-subtitle">' + esc(e.institution) + (e.grade ? ' | CGPA: ' + esc(e.grade) : '') + '</div>';
                if (e.description) html += '<div class="preview-entry-desc">' + esc(e.description) + '</div>';
                html += '</div>';
            });
            html += '</div>';
        }
        // Experience
        if (s === 'experience' && d.experience.length > 0) {
            html += '<div class="preview-section"><div class="preview-section-title" style="color:' + c + ';">Experience</div>';
            d.experience.forEach(e => {
                html += '<div class="preview-entry">';
                html += '<div class="preview-entry-header"><span class="preview-entry-title">' + esc(e.jobTitle) + '</span>';
                html += '<span class="preview-entry-date">' + esc(e.startDate) + (e.currentlyWorking ? ' - Present' : (e.endDate ? ' - ' + esc(e.endDate) : '')) + '</span></div>';
                html += '<div class="preview-entry-subtitle">' + esc(e.company) + (e.location ? ', ' + esc(e.location) : '') + (e.employmentType ? ' | ' + esc(e.employmentType) : '') + '</div>';
                if (e.description) html += '<div class="preview-entry-desc">' + esc(e.description) + '</div>';
                if (e.achievements) html += '<div class="preview-entry-desc" style="margin-top:2px;"><strong>Achievements:</strong> ' + esc(e.achievements) + '</div>';
                html += '</div>';
            });
            html += '</div>';
        }
        // Skills
        if (s === 'skills' && d.skills.length > 0) {
            html += '<div class="preview-section"><div class="preview-section-title" style="color:' + c + ';">Skills</div>';
            html += '<div class="preview-skills">';
            d.skills.forEach(sk => {
                if (sk.name) html += '<span class="preview-skill-tag" style="background:' + c + '15;color:' + c + ';">' + esc(sk.name) + '</span>';
            });
            html += '</div></div>';
        }
        // Projects
        if (s === 'projects' && d.projects.length > 0) {
            html += '<div class="preview-section"><div class="preview-section-title" style="color:' + c + ';">Projects</div>';
            d.projects.forEach(p => {
                html += '<div class="preview-entry">';
                html += '<div class="preview-entry-header"><span class="preview-entry-title">' + esc(p.name) + '</span>';
                if (p.startDate) html += '<span class="preview-entry-date">' + esc(p.startDate) + (p.endDate ? ' - ' + esc(p.endDate) : '') + '</span>';
                html += '</div>';
                if (p.role) html += '<div class="preview-entry-subtitle">' + esc(p.role) + '</div>';
                if (p.description) html += '<div class="preview-entry-desc">' + esc(p.description) + '</div>';
                if (p.technologies) {
                    html += '<div style="margin-top:3px;">';
                    p.technologies.split(',').forEach(t => {
                        if (t.trim()) html += '<span class="preview-skill-tag" style="background:' + c + '15;color:' + c + ';font-size:7.5pt;">' + esc(t.trim()) + '</span> ';
                    });
                    html += '</div>';
                }
                if (p.keyContributions) html += '<div class="preview-entry-desc" style="margin-top:2px;"><strong>Key Contributions:</strong> ' + esc(p.keyContributions) + '</div>';
                const links = [];
                if (p.githubUrl) links.push('<a href="' + esc(p.githubUrl) + '" style="color:' + c + ';font-size:8pt;">GitHub</a>');
                if (p.liveDemoUrl) links.push('<a href="' + esc(p.liveDemoUrl) + '" style="color:' + c + ';font-size:8pt;">Live Demo</a>');
                if (links.length) html += '<div style="margin-top:3px;gap:10px;">' + links.join(' | ') + '</div>';
                html += '</div>';
            });
            html += '</div>';
        }
        // Certifications
        if (s === 'certifications' && d.certifications.length > 0) {
            html += '<div class="preview-section"><div class="preview-section-title" style="color:' + c + ';">Certifications</div>';
            d.certifications.forEach(cr => {
                html += '<div class="preview-entry">';
                html += '<div class="preview-entry-header"><span class="preview-entry-title">' + esc(cr.name) + '</span>';
                if (cr.date) html += '<span class="preview-entry-date">' + esc(cr.date) + '</span>';
                html += '</div>';
                if (cr.issuingOrganization) html += '<div class="preview-entry-subtitle">' + esc(cr.issuingOrganization) + (cr.credentialId ? ' | ID: ' + esc(cr.credentialId) : '') + '</div>';
                if (cr.credentialUrl) html += '<div style="margin-top:2px;"><a href="' + esc(cr.credentialUrl) + '" style="color:' + c + ';font-size:8.5pt;">View Credential</a></div>';
                html += '</div>';
            });
            html += '</div>';
        }
        // Achievements
        if (s === 'achievements' && d.achievements.length > 0) {
            html += '<div class="preview-section"><div class="preview-section-title" style="color:' + c + ';">Achievements</div>';
            d.achievements.forEach(a => {
                html += '<div class="preview-entry">';
                html += '<div class="preview-entry-header"><span class="preview-entry-title">' + esc(a.title) + '</span>';
                if (a.date) html += '<span class="preview-entry-date">' + esc(a.date) + '</span>';
                html += '</div>';
                if (a.description) html += '<div class="preview-entry-desc">' + esc(a.description) + '</div>';
                html += '</div>';
            });
            html += '</div>';
        }
        // Languages
        if (s === 'languages' && d.languages.length > 0) {
            html += '<div class="preview-section"><div class="preview-section-title" style="color:' + c + ';">Languages</div>';
            html += '<div style="display:flex;flex-wrap:wrap;gap:8px;">';
            d.languages.forEach(l => {
                if (l.name) html += '<span style="font-size:9pt;color:#333;">' + esc(l.name) + (l.proficiency ? ' <span style="color:#777;">(' + esc(l.proficiency) + ')</span>' : '') + '</span>';
            });
            html += '</div></div>';
        }
    }

    // Empty state
    if (!d.summary && d.education.length === 0 && d.experience.length === 0 && d.skills.length === 0 && d.projects.length === 0) {
        html += '<div style="text-align:center;color:#aaa;padding:40px 0;font-size:10pt;">Start filling in your resume sections to see a live preview.</div>';
    }

    document.getElementById('resumePreview').innerHTML = html;
}

// ===== Template & Customization =====
function setTemplate(id, el) {
    templateId = id;
    document.querySelectorAll('.template-card').forEach(c => c.classList.remove('active'));
    el.classList.add('active');
    updatePreview();
    showSaveToast('Template: ' + (el.querySelector('h5')?.textContent || id));
}

let currentCategory = 'all';
function filterByCategory(cat, el) {
    currentCategory = cat;
    document.querySelectorAll('.template-filter').forEach(b => b.classList.remove('active'));
    el.classList.add('active');
    filterTemplates();
}
function filterTemplates() {
    const q = (document.getElementById('templateSearch')?.value || '').toLowerCase();
    document.querySelectorAll('#templateGrid .template-card').forEach(card => {
        const name = (card.querySelector('h5')?.textContent || '').toLowerCase();
        const cat = (card.dataset.cat || '').toLowerCase();
        const matchSearch = !q || name.includes(q) || cat.includes(q);
        const matchCat = currentCategory === 'all' || cat.includes(currentCategory.toLowerCase());
        card.style.display = (matchSearch && matchCat) ? '' : 'none';
    });
}
function setColor(color, el) {
    primaryColor = color;
    document.querySelectorAll('.color-option').forEach(c => c.classList.remove('active'));
    el.classList.add('active');
    updatePreview();
}
function setFont(font) { fontFamily = font; updatePreview(); }
function setFontSize(size) { fontSize = size; document.getElementById('fontSizeLabel').textContent = size; updatePreview(); }
function updateCharCount() { document.getElementById('charCount').textContent = val('summary').length; }

// ===== CRUD: Education =====
function addEducation() {
    fetch('/builder/' + resumeId + '/education', {
        method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify({institution:'',degree:'',field:'',startDate:'',endDate:'',grade:'',description:'',sortOrder:0})
    }).then(r=>r.json()).then(d=>{
        if(d.success && d.item) {
            const id = d.item.id;
            const card = document.createElement('div');
            card.className = 'card mb-2';
            card.id = 'edu-' + id;
            card.innerHTML = '<div class="flex-between mb-1"><strong>New Education</strong><button class="btn btn-danger btn-sm" onclick="deleteEducation('+id+')">Delete</button></div>'
                +'<div class="form-row"><div class="form-group"><label>Institution</label><input type="text" class="form-control" data-id="'+id+'" data-field="institution" oninput="updateEducationField(this)"></div>'
                +'<div class="form-group"><label>Degree</label><input type="text" class="form-control" data-id="'+id+'" data-field="degree" oninput="updateEducationField(this)"></div></div>'
                +'<div class="form-row"><div class="form-group"><label>Field of Study</label><input type="text" class="form-control" data-id="'+id+'" data-field="field" oninput="updateEducationField(this)"></div>'
                +'<div class="form-group"><label>Grade/CGPA</label><input type="text" class="form-control" data-id="'+id+'" data-field="grade" oninput="updateEducationField(this)"></div></div>'
                +'<div class="form-row"><div class="form-group"><label>Start Date</label><input type="text" class="form-control" placeholder="e.g. Aug 2022" data-id="'+id+'" data-field="startDate" oninput="updateEducationField(this)"></div>'
                +'<div class="form-group"><label>End Date</label><input type="text" class="form-control" placeholder="e.g. May 2026" data-id="'+id+'" data-field="endDate" oninput="updateEducationField(this)"></div></div>'
                +'<div class="form-group"><label>Description</label><textarea class="form-control" rows="2" data-id="'+id+'" data-field="description" oninput="updateEducationField(this)"></textarea></div>';
            document.getElementById('education-list').appendChild(card);
            showSaveToast('Education added!');
        } else { alert(d.error||'Failed'); }
    });
}
function updateEducationField(el) {
    const id = el.dataset.id;
    const data = {sortOrder:0};
    const card = el.closest('.card');
    card.querySelectorAll('input, textarea').forEach(inp => { if(inp.dataset.field) data[inp.dataset.field] = inp.value; });
    fetch('/builder/education/' + id, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(data)});
    updatePreview();
}
function deleteEducation(id) {
    if(!confirm('Delete this education?')) return;
    fetch('/builder/education/' + id, {method:'DELETE'}).then(()=>{
        const el = document.getElementById('edu-' + id);
        if(el) el.remove();
        updatePreview();
        showSaveToast('Education deleted!');
    });
}

// ===== CRUD: Experience =====
function addExperience() {
    fetch('/builder/' + resumeId + '/experience', {
        method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify({jobTitle:'',company:'',location:'',employmentType:'',startDate:'',endDate:'',currentlyWorking:false,description:'',achievements:'',sortOrder:0})
    }).then(r=>r.json()).then(d=>{
        if(d.success && d.item) {
            const id = d.item.id;
            const card = document.createElement('div');
            card.className = 'card mb-2';
            card.id = 'exp-' + id;
            card.innerHTML = '<div class="flex-between mb-1"><strong>New Experience</strong><button class="btn btn-danger btn-sm" onclick="deleteExperience('+id+')">Delete</button></div>'
                +'<div class="form-row"><div class="form-group"><label>Job Title</label><input type="text" class="form-control" data-id="'+id+'" data-field="jobTitle" oninput="updateExpField(this)"></div>'
                +'<div class="form-group"><label>Company</label><input type="text" class="form-control" data-id="'+id+'" data-field="company" oninput="updateExpField(this)"></div></div>'
                +'<div class="form-row"><div class="form-group"><label>Location</label><input type="text" class="form-control" data-id="'+id+'" data-field="location" oninput="updateExpField(this)"></div>'
                +'<div class="form-group"><label>Employment Type</label><select class="form-control" data-id="'+id+'" data-field="employmentType" onchange="updateExpField(this)"><option value="">Select</option><option value="Full-time">Full-time</option><option value="Part-time">Part-time</option><option value="Internship">Internship</option><option value="Contract">Contract</option><option value="Freelance">Freelance</option></select></div></div>'
                +'<div class="form-row"><div class="form-group"><label>Currently Working</label><select class="form-control" data-id="'+id+'" data-field="currentlyWorking" onchange="updateExpField(this)"><option value="false">No</option><option value="true">Yes</option></select></div>'
                +'<div class="form-group"><label>Start Date</label><input type="text" class="form-control" placeholder="e.g. Jan 2024" data-id="'+id+'" data-field="startDate" oninput="updateExpField(this)"></div></div>'
                +'<div class="form-row"><div class="form-group"><label>End Date</label><input type="text" class="form-control" placeholder="e.g. Present" data-id="'+id+'" data-field="endDate" oninput="updateExpField(this)"></div><div class="form-group"></div></div>'
                +'<div class="form-group"><label>Description</label><textarea class="form-control" rows="3" data-id="'+id+'" data-field="description" oninput="updateExpField(this)"></textarea></div>'
                +'<div class="form-group"><label>Key Achievements</label><textarea class="form-control" rows="2" data-id="'+id+'" data-field="achievements" oninput="updateExpField(this)" placeholder="e.g. Increased performance by 40%"></textarea></div>';
            document.getElementById('experience-list').appendChild(card);
            showSaveToast('Experience added!');
        } else { alert(d.error||'Failed'); }
    });
}
function updateExpField(el) {
    const id = el.dataset.id;
    const data = {sortOrder:0, currentlyWorking:false};
    const card = el.closest('.card');
    card.querySelectorAll('input, textarea, select').forEach(inp => { if(inp.dataset.field) data[inp.dataset.field] = inp.value; });
    data.currentlyWorking = (data.currentlyWorking === 'true' || data.currentlyWorking === true);
    fetch('/builder/experience/' + id, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(data)});
    updatePreview();
}
function deleteExperience(id) {
    if(!confirm('Delete this experience?')) return;
    fetch('/builder/experience/' + id, {method:'DELETE'}).then(()=>{
        const el = document.getElementById('exp-' + id);
        if(el) el.remove();
        updatePreview();
        showSaveToast('Experience deleted!');
    });
}

// ===== CRUD: Skills =====
function addSkill() {
    fetch('/builder/' + resumeId + '/skills', {
        method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify({name:'',category:'',proficiency:'Intermediate',sortOrder:0})
    }).then(r=>r.json()).then(d=>{
        if(d.success && d.item) {
            const id = d.item.id;
            const card = document.createElement('div');
            card.className = 'card mb-2 flex-between';
            card.id = 'skill-' + id;
            card.innerHTML = '<div style="flex:1;"><div class="form-row"><div class="form-group"><label>Skill Name</label><input type="text" class="form-control" data-id="'+id+'" data-field="name" oninput="updateSkillField(this)"></div>'
                +'<div class="form-group"><label>Category</label><input type="text" class="form-control" placeholder="e.g. Programming" data-id="'+id+'" data-field="category" oninput="updateSkillField(this)"></div></div></div>'
                +'<button class="btn btn-danger btn-sm" style="margin-left:8px;" onclick="deleteSkill('+id+')">Delete</button>';
            document.getElementById('skills-list').appendChild(card);
            showSaveToast('Skill added!');
        } else { alert(d.error||'Failed'); }
    });
}
function updateSkillField(el) {
    const id = el.dataset.id;
    const data = {sortOrder:0, proficiency:'Intermediate'};
    const card = el.closest('.card');
    card.querySelectorAll('input').forEach(inp => { if(inp.dataset.field) data[inp.dataset.field] = inp.value; });
    fetch('/builder/skills/' + id, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(data)});
    updatePreview();
}
function deleteSkill(id) {
    if(!confirm('Delete this skill?')) return;
    fetch('/builder/skills/' + id, {method:'DELETE'}).then(()=>{
        const el = document.getElementById('skill-' + id);
        if(el) el.remove();
        updatePreview();
        showSaveToast('Skill deleted!');
    });
}

// ===== CRUD: Projects =====
function addProject() {
    fetch('/builder/' + resumeId + '/projects', {
        method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify({name:'',description:'',technologies:'',role:'',startDate:'',endDate:'',keyContributions:'',githubUrl:'',liveDemoUrl:'',sortOrder:0})
    }).then(r=>r.json()).then(d=>{
        if(d.success && d.item) {
            const id = d.item.id;
            const card = document.createElement('div');
            card.className = 'card mb-2';
            card.id = 'proj-' + id;
            card.innerHTML = '<div class="flex-between mb-1"><strong>New Project</strong><button class="btn btn-danger btn-sm" onclick="deleteProject('+id+')">Delete</button></div>'
                +'<div class="form-row"><div class="form-group"><label>Project Name</label><input type="text" class="form-control" data-id="'+id+'" data-field="name" oninput="updateProjectField(this)"></div>'
                +'<div class="form-group"><label>Role</label><input type="text" class="form-control" placeholder="e.g. Lead Developer" data-id="'+id+'" data-field="role" oninput="updateProjectField(this)"></div></div>'
                +'<div class="form-row"><div class="form-group"><label>Technologies</label><input type="text" class="form-control" placeholder="e.g. Java, Spring Boot, MySQL" data-id="'+id+'" data-field="technologies" oninput="updateProjectField(this)"></div>'
                +'<div class="form-group"><label>Duration</label><input type="text" class="form-control" placeholder="e.g. Jan 2024 - Jun 2024" data-id="'+id+'" data-field="startDate" oninput="updateProjectField(this)"></div></div>'
                +'<div class="form-group"><label>Description</label><textarea class="form-control" rows="2" data-id="'+id+'" data-field="description" oninput="updateProjectField(this)"></textarea></div>'
                +'<div class="form-group"><label>Key Contributions</label><textarea class="form-control" rows="2" data-id="'+id+'" data-field="keyContributions" oninput="updateProjectField(this)" placeholder="Key technical decisions, optimizations"></textarea></div>'
                +'<div class="form-row"><div class="form-group"><label>GitHub URL</label><input type="url" class="form-control" data-id="'+id+'" data-field="githubUrl" oninput="updateProjectField(this)"></div>'
                +'<div class="form-group"><label>Live Demo URL</label><input type="url" class="form-control" data-id="'+id+'" data-field="liveDemoUrl" oninput="updateProjectField(this)"></div></div>';
            document.getElementById('projects-list').appendChild(card);
            showSaveToast('Project added!');
        } else { alert(d.error||'Failed'); }
    });
}
function updateProjectField(el) {
    const id = el.dataset.id;
    const data = {sortOrder:0};
    const card = el.closest('.card');
    card.querySelectorAll('input, textarea').forEach(inp => { if(inp.dataset.field) data[inp.dataset.field] = inp.value; });
    fetch('/builder/projects/' + id, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(data)});
    updatePreview();
}
function deleteProject(id) { if(!confirm('Delete this project?')) return; fetch('/builder/projects/' + id, {method:'DELETE'}).then(()=>{ const el=document.getElementById('proj-'+id); if(el) el.remove(); updatePreview(); showSaveToast('Project deleted!'); }); }

// ===== CRUD: Certifications =====
function addCertification() {
    fetch('/builder/' + resumeId + '/certifications', {
        method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify({name:'',issuingOrganization:'',date:'',credentialId:'',credentialUrl:'',sortOrder:0})
    }).then(r=>r.json()).then(d=>{
        if(d.success && d.item) {
            const id = d.item.id;
            const card = document.createElement('div');
            card.className = 'card mb-2 flex-between';
            card.id = 'cert-' + id;
            card.innerHTML = '<div style="flex:1;">'
                +'<div class="form-row"><div class="form-group"><label>Certification Name</label><input type="text" class="form-control" data-id="'+id+'" data-field="name" oninput="updateCertField(this)"></div>'
                +'<div class="form-group"><label>Issuing Organization</label><input type="text" class="form-control" data-id="'+id+'" data-field="issuingOrganization" oninput="updateCertField(this)"></div></div>'
                +'<div class="form-row"><div class="form-group"><label>Date</label><input type="text" class="form-control" data-id="'+id+'" data-field="date" oninput="updateCertField(this)"></div>'
                +'<div class="form-group"><label>Credential ID</label><input type="text" class="form-control" data-id="'+id+'" data-field="credentialId" oninput="updateCertField(this)" placeholder="Optional"></div></div>'
                +'<div class="form-group"><label>Credential URL</label><input type="url" class="form-control" data-id="'+id+'" data-field="credentialUrl" oninput="updateCertField(this)" placeholder="https://"></div>'
                +'</div><button class="btn btn-danger btn-sm" style="margin-left:8px;" onclick="deleteCertification('+id+')">Delete</button>';
            document.getElementById('certifications-list').appendChild(card);
            showSaveToast('Certification added!');
        } else { alert(d.error||'Failed'); }
    });
}
function updateCertField(el) {
    const id = el.dataset.id;
    const data = {sortOrder:0};
    const card = el.closest('.card');
    card.querySelectorAll('input').forEach(inp => { if(inp.dataset.field) data[inp.dataset.field] = inp.value; });
    fetch('/builder/certifications/' + id, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(data)});
    updatePreview();
}
function deleteCertification(id) { if(!confirm('Delete this certification?')) return; fetch('/builder/certifications/' + id, {method:'DELETE'}).then(()=>{ const el=document.getElementById('cert-'+id); if(el) el.remove(); updatePreview(); showSaveToast('Certification deleted!'); }); }

// ===== CRUD: Achievements =====
function addAchievement() {
    fetch('/builder/' + resumeId + '/achievements', {
        method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify({title:'',description:'',date:'',sortOrder:0})
    }).then(r=>r.json()).then(d=>{
        if(d.success && d.item) {
            const id = d.item.id;
            const card = document.createElement('div');
            card.className = 'card mb-2 flex-between';
            card.id = 'ach-' + id;
            card.innerHTML = '<div style="flex:1;">'
                +'<div class="form-group"><label>Title</label><input type="text" class="form-control" data-id="'+id+'" data-field="title" oninput="updateAchField(this)"></div>'
                +'<div class="form-group"><label>Description</label><textarea class="form-control" rows="2" data-id="'+id+'" data-field="description" oninput="updateAchField(this)"></textarea></div>'
                +'</div><button class="btn btn-danger btn-sm" style="margin-left:8px;" onclick="deleteAchievement('+id+')">Delete</button>';
            document.getElementById('achievements-list').appendChild(card);
            showSaveToast('Achievement added!');
        } else { alert(d.error||'Failed'); }
    });
}
function updateAchField(el) {
    const id = el.dataset.id;
    const data = {sortOrder:0};
    const card = el.closest('.card');
    card.querySelectorAll('input, textarea').forEach(inp => { if(inp.dataset.field) data[inp.dataset.field] = inp.value; });
    fetch('/builder/achievements/' + id, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(data)});
    updatePreview();
}
function deleteAchievement(id) { if(!confirm('Delete this achievement?')) return; fetch('/builder/achievements/' + id, {method:'DELETE'}).then(()=>{ const el=document.getElementById('ach-'+id); if(el) el.remove(); updatePreview(); showSaveToast('Achievement deleted!'); }); }

// ===== CRUD: Languages =====
function addLanguage() {
    fetch('/builder/' + resumeId + '/languages', {
        method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify({name:'',proficiency:'Conversational',sortOrder:0})
    }).then(r=>r.json()).then(d=>{
        if(d.success && d.item) {
            const id = d.item.id;
            const card = document.createElement('div');
            card.className = 'card mb-2 flex-between';
            card.id = 'lang-' + id;
            card.innerHTML = '<div style="flex:1;"><div class="form-row"><div class="form-group"><label>Language</label><input type="text" class="form-control" data-id="'+id+'" data-field="name" oninput="updateLangField(this)"></div>'
                +'<div class="form-group"><label>Proficiency</label><select class="form-control" data-id="'+id+'" data-field="proficiency" onchange="updateLangField(this)"><option value="Native">Native</option><option value="Fluent">Fluent</option><option value="Advanced">Advanced</option><option value="Conversational" selected>Conversational</option><option value="Basic">Basic</option></select></div></div></div>'
                +'<button class="btn btn-danger btn-sm" style="margin-left:8px;" onclick="deleteLanguage('+id+')">Delete</button>';
            document.getElementById('languages-list').appendChild(card);
            showSaveToast('Language added!');
        } else { alert(d.error||'Failed'); }
    });
}
function updateLangField(el) {
    const id = el.dataset.id;
    const data = {sortOrder:0};
    const card = el.closest('.card');
    card.querySelectorAll('input, select').forEach(inp => { if(inp.dataset.field) data[inp.dataset.field] = inp.value; });
    fetch('/builder/languages/' + id, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(data)});
    updatePreview();
}
function deleteLanguage(id) { if(!confirm('Delete this language?')) return; fetch('/builder/languages/' + id, {method:'DELETE'}).then(()=>{ const el=document.getElementById('lang-'+id); if(el) el.remove(); updatePreview(); showSaveToast('Language deleted!'); }); }

// ===== Social Links =====
function addSocialLink() {
    fetch('/builder/' + resumeId + '/sociallinks', {
        method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify({platform:'GitHub',url:''})
    }).then(r=>r.json()).then(d=>{
        if(d.success && d.item) {
            const id = d.item.id;
            const card = document.createElement('div');
            card.className = 'card mb-2 flex-between';
            card.id = 'sl-' + id;
            card.innerHTML = '<div style="flex:1;"><div class="form-row"><div class="form-group"><label>Platform</label><select class="form-control" data-id="'+id+'" data-field="platform" onchange="updateSocialLinkField(this)"><option value="GitHub">GitHub</option><option value="LinkedIn">LinkedIn</option><option value="Portfolio">Portfolio</option><option value="Twitter">Twitter</option><option value="Other">Other</option></select></div>'
                +'<div class="form-group"><label>URL</label><input type="url" class="form-control" data-id="'+id+'" data-field="url" oninput="updateSocialLinkField(this)" placeholder="https://"></div></div></div>'
                +'<button class="btn btn-danger btn-sm" style="margin-left:8px;" onclick="deleteSocialLink('+id+')">Delete</button>';
            document.getElementById('sociallinks-list').appendChild(card);
            showSaveToast('Link added!');
        } else { alert(d.error||'Failed'); }
    });
}
function updateSocialLinkField(el) {
    const id = el.dataset.id;
    const data = {};
    const card = el.closest('.card');
    card.querySelectorAll('input, select').forEach(inp => { if(inp.dataset.field) data[inp.dataset.field] = inp.value; });
    fetch('/builder/sociallinks/' + id, {method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(data)});
    updatePreview();
}
function deleteSocialLink(id) { if(!confirm('Delete this social link?')) return; fetch('/builder/sociallinks/' + id, {method:'DELETE'}).then(()=>{ const el=document.getElementById('sl-'+id); if(el) el.remove(); updatePreview(); showSaveToast('Link deleted!'); }); }

// ===== Score =====
function calculateScore() {
    document.getElementById('score-result').innerHTML = '<p class="text-muted">Calculating...</p>';
    setTimeout(() => {
        const checks = [
            {name:'Personal Info', score: (val('firstName') && val('lastName') && val('personalEmail') && val('phone')) ? 20 : 0, max:20},
            {name:'Summary', score: val('summary').length > 50 ? 15 : val('summary').length > 0 ? 8 : 0, max:15},
            {name:'Education', score: Math.min(15, document.querySelectorAll('#education-list .card').length * 15), max:15},
            {name:'Experience', score: Math.min(15, document.querySelectorAll('#experience-list .card').length * 15), max:15},
            {name:'Skills', score: Math.min(15, document.querySelectorAll('#skills-list .card').length * 3), max:15},
            {name:'Projects', score: Math.min(10, document.querySelectorAll('#projects-list .card').length * 5), max:10},
            {name:'Certifications', score: Math.min(5, document.querySelectorAll('#certifications-list .card').length * 5), max:5},
            {name:'Social Links', score: (val('linkedin')?3:0) + (val('github')?2:0), max:5}
        ];
        const total = checks.reduce((a,c) => a + c.score, 0);
        let html = '<div style="text-align:center;margin-bottom:20px;">';
        html += '<div style="font-size:3rem;font-weight:800;color:' + primaryColor + ';">' + total + '%</div>';
        html += '<div style="font-size:1rem;color:var(--gray-500);">' + (total>=90?'Excellent':total>=70?'Good':total>=50?'Fair':'Needs Work') + '</div></div>';
        checks.forEach(c => {
            const pct = (c.score*100)/c.max;
            const cls = pct>=80?'green':pct>0?'amber':'red';
            html += '<div style="display:flex;align-items:center;gap:10px;margin-bottom:8px;"><span style="width:120px;font-size:0.85rem;font-weight:500;">' + c.name + '</span>';
            html += '<div class="progress-bar" style="flex:1;"><div class="progress-fill ' + cls + '" style="width:' + pct + '%;"></div></div>';
            html += '<span style="font-size:0.8rem;color:var(--gray-500);width:40px;text-align:right;">' + c.score + '/' + c.max + '</span></div>';
        });
        html += '<button class="btn btn-primary mt-2" onclick="calculateScore()">Recalculate</button>';
        document.getElementById('score-result').innerHTML = html;
    }, 300);
}

// ===== ATS Check =====
function runAtsCheck() {
    document.getElementById('ats-result').innerHTML = '<p class="text-muted">Analyzing...</p>';
    setTimeout(() => {
        const items = [];
        const hasContact = val('firstName') && val('lastName') && val('personalEmail') && val('phone');
        items.push({title:'Contact Info', status: hasContact?'pass':'fail', detail: hasContact?'Complete':'Add name, email, phone'});
        items.push({title:'Email Format', status: val('personalEmail').includes('@')?'pass':'fail', detail: val('personalEmail').includes('@')?'Valid':'Invalid email'});
        const hasSummary = val('summary').length > 50;
        items.push({title:'Professional Summary', status: hasSummary?'pass':val('summary').length>0?'warn':'fail', detail: hasSummary?'Good length':'Add 50+ characters'});
        const skillCount = document.querySelectorAll('#skills-list .card').length;
        items.push({title:'Skills', status: skillCount>=5?'pass':skillCount>0?'warn':'fail', detail: skillCount+' skills listed'});
        items.push({title:'Education', status: document.querySelectorAll('#education-list .card').length>0?'pass':'warn', detail: document.querySelectorAll('#education-list .card').length + ' entries'});
        items.push({title:'Experience', status: document.querySelectorAll('#experience-list .card').length>0?'pass':'warn', detail: document.querySelectorAll('#experience-list .card').length + ' entries'});
        items.push({title:'Projects', status: document.querySelectorAll('#projects-list .card').length>0?'pass':'warn', detail: document.querySelectorAll('#projects-list .card').length + ' entries'});
        let html = '<div style="margin-bottom:12px;font-weight:600;">ATS Score: ' + Math.round(items.filter(i=>i.status==='pass').length/items.length*100) + '%</div>';
        items.forEach(i => {
            const icons = {pass:'&#9989;', fail:'&#10060;', warn:'&#9888;'};
            html += '<div class="ats-item ' + i.status + '"><span class="ats-icon">' + icons[i.status] + '</span><div><div class="ats-item-title">' + i.title + '</div><div class="ats-item-detail">' + i.detail + '</div></div></div>';
        });
        html += '<button class="btn btn-primary mt-2" onclick="runAtsCheck()">Re-run Check</button>';
        document.getElementById('ats-result').innerHTML = html;
    }, 300);
}

// ===== AI Inline Improvement =====
function aiImproveSection(section) {
    const jobId = '';
    let text = '';
    let action = 'chat';
    
    switch(section) {
        case 'summary':
            text = val('summary');
            if (!text) { alert('Please enter a summary first.'); return; }
            action = 'improve-summary';
            break;
        case 'experience':
            // Get the first experience description if available
            const expCards = document.querySelectorAll('#experience-list .card');
            if (expCards.length === 0) { alert('Please add an experience entry first.'); return; }
            const expDesc = expCards[0].querySelector('[data-field="description"]');
            text = expDesc ? expDesc.value : '';
            if (!text) { alert('Please enter an experience description first.'); return; }
            action = 'improve-experience';
            break;
        case 'projects':
            const projCards = document.querySelectorAll('#projects-list .card');
            if (projCards.length === 0) { alert('Please add a project first.'); return; }
            const projDesc = projCards[0].querySelector('[data-field="description"]');
            text = projDesc ? projDesc.value : '';
            if (!text) { alert('Please enter a project description first.'); return; }
            action = 'improve-project';
            break;
    }
    
    // Show loading modal
    showAiModal('Analyzing with AI...', 'Please wait...', true);
    
    fetch('/ai-assistant/chat', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            message: text,
            resumeId: resumeId,
            jobDescription: '',
            sectionText: text,
            action: action
        })
    }).then(r => r.json()).then(data => {
        if (data.success) {
            let suggestion = data.result || data.reply || '';
            showAiSuggestionModal(section, text, suggestion);
        } else {
            showAiModal('Error', data.error || 'AI is temporarily unavailable.', false);
        }
    }).catch(err => {
        showAiModal('Error', 'Connection failed. Please try again.', false);
    });
}

function showAiModal(title, message, loading) {
    let modal = document.getElementById('aiModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'aiModal';
        modal.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:10000;display:flex;align-items:center;justify-content:center;';
        document.body.appendChild(modal);
    }
    let html = '<div style="background:#fff;border-radius:12px;padding:24px;max-width:500px;width:90%;max-height:80vh;overflow-y:auto;box-shadow:0 20px 60px rgba(0,0,0,0.3);">';
    html += '<h3 style="margin-bottom:12px;">' + esc(title) + '</h3>';
    if (loading) {
        html += '<div style="display:flex;align-items:center;gap:8px;color:#6366f1;"><div style="width:20px;height:20px;border:3px solid #6366f1;border-top-color:transparent;border-radius:50%;animation:spin 1s linear infinite;"></div>' + esc(message) + '</div>';
    } else {
        html += '<p style="color:#555;">' + esc(message) + '</p>';
    }
    html += '<div style="text-align:right;margin-top:16px;"><button onclick="closeAiModal()" class="btn btn-primary btn-sm">Close</button></div>';
    html += '</div>';
    modal.innerHTML = html;
}

function showAiSuggestionModal(section, original, suggested) {
    let modal = document.getElementById('aiModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'aiModal';
        modal.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:10000;display:flex;align-items:center;justify-content:center;';
        document.body.appendChild(modal);
    }
    let html = '<div style="background:#fff;border-radius:12px;padding:24px;max-width:600px;width:90%;max-height:80vh;overflow-y:auto;box-shadow:0 20px 60px rgba(0,0,0,0.3);">';
    html += '<h3 style="margin-bottom:4px;color:#6366f1;">✨ AI Suggestion</h3>';
    html += '<p style="font-size:12px;color:#888;margin-bottom:16px;">Review the suggestion below. Accept to apply it to your resume.</p>';
    html += '<div style="background:#f8f9fa;border-radius:8px;padding:12px;margin-bottom:12px;"><div style="font-size:11px;font-weight:600;color:#666;margin-bottom:4px;">ORIGINAL</div><p style="font-size:13px;color:#333;">' + esc(original) + '</p></div>';
    html += '<div style="background:#f0f0ff;border-radius:8px;padding:12px;margin-bottom:16px;border-left:3px solid #6366f1;"><div style="font-size:11px;font-weight:600;color:#6366f1;margin-bottom:4px;">AI SUGGESTION</div><p style="font-size:13px;color:#333;">' + esc(suggested) + '</p></div>';
    html += '<div style="display:flex;gap:8px;justify-content:flex-end;">';
    html += '<button onclick="closeAiModal()" class="btn btn-secondary btn-sm" style="padding:8px 16px;">Reject</button>';
    html += '<button onclick="aiAcceptSuggestion(\'' + section + '\', \'' + esc(suggested).replace(/'/g, "\\'") + '\')" class="btn btn-primary btn-sm" style="padding:8px 16px;background:#6366f1;">Accept</button>';
    html += '</div>';
    html += '</div>';
    modal.innerHTML = html;
}

function aiAcceptSuggestion(section, text) {
    if (section === 'summary') {
        document.getElementById('summary').value = text;
    } else if (section === 'experience') {
        const firstDesc = document.querySelector('#experience-list .card [data-field="description"]');
        if (firstDesc) { firstDesc.value = text; firstDesc.dispatchEvent(new Event('input')); }
    } else if (section === 'projects') {
        const firstProjDesc = document.querySelector('#projects-list .card [data-field="description"]');
        if (firstProjDesc) { firstProjDesc.value = text; firstProjDesc.dispatchEvent(new Event('input')); }
    }
    updatePreview();
    closeAiModal();
    showSaveToast('AI suggestion applied!');
}

function closeAiModal() {
    const modal = document.getElementById('aiModal');
    if (modal) modal.remove();
}

// ===== AI Chatbot in Builder =====
let builderChatConversationId = localStorage.getItem('builderChatConversationId') || null;

async function sendBuilderChat() {
    const input = document.getElementById('builderChatInput');
    const messagesDiv = document.getElementById('builderChatMessages');
    const msg = input.value.trim();
    if (!msg) return;

    // Add user message
    messagesDiv.innerHTML += '<div style="background:#e5e7eb;padding:10px 14px;border-radius:12px 12px 0 12px;display:inline-block;max-width:90%;font-size:0.9rem;margin-bottom:8px;margin-left:10%;float:right;clear:both;">' + esc(msg) + '</div>';
    messagesDiv.innerHTML += '<div style="clear:both;"></div>';
    input.value = '';

    // Add loading indicator
    const loadingId = 'loading-' + Date.now();
    messagesDiv.innerHTML += '<div id="' + loadingId + '" style="background:var(--primary);color:#fff;padding:10px 14px;border-radius:12px 12px 12px 0;display:inline-block;max-width:90%;font-size:0.9rem;margin-bottom:8px;"><span style="animation:pulse 1s infinite;">Thinking...</span></div>';
    messagesDiv.scrollTop = messagesDiv.scrollHeight;

    // Build resume context
    const resumeData = readResumeData();
    const resumeContext = 'Resume: ' + (resumeData.firstName || '') + ' ' + (resumeData.lastName || '') + '\n'
        + 'Title: ' + (resumeData.professionalTitle || '') + '\n'
        + 'Summary: ' + (resumeData.summary || '') + '\n'
        + 'Education: ' + resumeData.education.map(e => e.degree + ' from ' + e.institution).join('; ') + '\n'
        + 'Experience: ' + resumeData.experience.map(e => e.jobTitle + ' at ' + e.company).join('; ') + '\n'
        + 'Skills: ' + resumeData.skills.map(s => s.name).join(', ') + '\n'
        + 'Projects: ' + resumeData.projects.map(p => p.name).join(', ');

    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ message: msg, resumeContext: resumeContext, conversationId: builderChatConversationId })
        });
        const data = await response.json();
        const loadingEl = document.getElementById(loadingId);
        if (loadingEl) loadingEl.remove();

        if (data.conversationId) {
            builderChatConversationId = data.conversationId;
            localStorage.setItem('builderChatConversationId', String(data.conversationId));
        }

        if (data.success && data.reply) {
            const formatted = data.reply
                .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                .replace(/\n/g, '<br>')
                .replace(/- (.*?)(<br>|$)/g, '• $1$2');
            messagesDiv.innerHTML += '<div style="background:var(--primary);color:#fff;padding:10px 14px;border-radius:12px 12px 12px 0;display:inline-block;max-width:90%;font-size:0.9rem;margin-bottom:8px;">' + formatted + '</div>';
        } else {
            messagesDiv.innerHTML += '<div style="background:var(--danger);color:#fff;padding:10px 14px;border-radius:12px 12px 12px 0;display:inline-block;max-width:90%;font-size:0.9rem;margin-bottom:8px;">' + esc(data.error || 'AI unavailable. Please try again.') + '</div>';
        }
    } catch (err) {
        const loadingEl2 = document.getElementById(loadingId);
        if (loadingEl2) loadingEl2.remove();
        messagesDiv.innerHTML += '<div style="background:var(--danger);color:#fff;padding:10px 14px;border-radius:12px 12px 12px 0;display:inline-block;max-width:90%;font-size:0.9rem;margin-bottom:8px;">Connection error. Please try again.</div>';
    }
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

// ===== Chat history restore (persistence) =====
function restoreBuilderChatHistory() {
    if (!builderChatConversationId) return;
    const messagesDiv = document.getElementById('builderChatMessages');
    if (!messagesDiv) return;
    fetch('/api/chat/conversations')
        .then(r => r.json())
        .then(list => {
            if (!list.success || !Array.isArray(list.conversations)) return;
            const conv = list.conversations.find(c => String(c.id) === String(builderChatConversationId));
            if (!conv) { // conversation belongs to another user or was cleared
                builderChatConversationId = null;
                localStorage.removeItem('builderChatConversationId');
                return;
            }
            fetch('/ai-assistant/conversation/' + builderChatConversationId + '/messages')
                .then(r => r.json())
                .then(data => {
                    if (!data.success || !Array.isArray(data.messages)) return;
                    data.messages.forEach(m => {
                        if (m.role === 'user') {
                            messagesDiv.innerHTML += '<div style="background:#e5e7eb;padding:10px 14px;border-radius:12px 12px 0 12px;display:inline-block;max-width:90%;font-size:0.9rem;margin-bottom:8px;margin-left:10%;float:right;clear:both;">' + esc(m.content) + '</div><div style="clear:both;"></div>';
                        } else {
                            const formatted = String(m.content)
                                .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                                .replace(/\n/g, '<br>')
                                .replace(/- (.*?)(<br>|$)/g, '• $1$2');
                            messagesDiv.innerHTML += '<div style="background:var(--primary);color:#fff;padding:10px 14px;border-radius:12px 12px 12px 0;display:inline-block;max-width:90%;font-size:0.9rem;margin-bottom:8px;">' + formatted + '</div>';
                        }
                    });
                    messagesDiv.scrollTop = messagesDiv.scrollHeight;
                })
                .catch(() => {});
        })
        .catch(() => {});
}

// ===== Init =====
updatePreview();
if (typeof restoreBuilderChatHistory === 'function') restoreBuilderChatHistory();
