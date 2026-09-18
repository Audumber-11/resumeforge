// ===== ResumeForge Template Library =====
// 10 Original Professional Resume Templates
// All templates use the same ResumeData model

// ===== Template Registry (must be first) =====
const templates = {};

// ===== Helper Functions =====
function _esc(s) { return (s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
function _contactHtml(d, c, style) {
    let h = '<div class="contact" ' + (style||'') + '>';
    if (d.personalEmail) h += '<span>' + _esc(d.personalEmail) + '</span>';
    if (d.phone) h += '<span>' + _esc(d.phone) + '</span>';
    if (d.location) h += '<span>' + _esc(d.location) + '</span>';
    h += '</div>';
    if (d.linkedin || d.github || d.website) {
        h += '<div class="contact-links">';
        if (d.linkedin) h += '<a href="' + _esc(d.linkedin) + '" style="color:' + c + ';margin-right:10px;text-decoration:none;">LinkedIn</a>';
        if (d.github) h += '<a href="' + _esc(d.github) + '" style="color:' + c + ';margin-right:10px;text-decoration:none;">GitHub</a>';
        if (d.website) h += '<a href="' + _esc(d.website) + '" style="color:' + c + ';margin-right:10px;text-decoration:none;">Portfolio</a>';
        h += '</div>';
    }
    return h;
}
function _sectionTitle(title, c, style) {
    return '<div class="tpl-section-title" style="' + (style||'') + 'color:' + c + ';">' + _esc(title) + '</div>';
}

// ====================================================================
// TEMPLATE 1: EXECUTIVE
// Single-column, elegant, thin dividers, large name
// ATS-friendly
// ====================================================================
templates['executive'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-executive">';
    // Header
    h += '<div class="tpl-exec-header" style="border-bottom: 2px solid ' + c + ';">';
    h += '<h1 style="font-family:' + ff + ';color:' + c + ';font-size:' + (fs+10) + 'pt;font-weight:700;letter-spacing:1px;margin:0 0 4px;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+1) + 'pt;color:#444;font-weight:400;margin-bottom:8px;">' + _esc(d.professionalTitle) + '</div>';
    h += _contactHtml(d, c);
    h += '</div>';
    // Sections
    h += _renderSectionsExec(d, c, ff, fs);
    h += '</div>';
    return h;
};
function _renderSectionsExec(d, c, ff, fs) {
    let h = '';
    if (d.summary) {
        h += '<div class="tpl-exec-section">';
        h += _sectionTitle('Professional Summary', c, 'border-bottom:1px solid #ddd;padding-bottom:4px;margin-bottom:8px;');
        h += '<p style="font-size:' + (fs-0.5) + 'pt;color:#333;line-height:1.6;">' + _esc(d.summary) + '</p></div>';
    }
    if (d.education.length > 0) {
        h += '<div class="tpl-exec-section">';
        h += _sectionTitle('Education', c, 'border-bottom:1px solid #ddd;padding-bottom:4px;margin-bottom:8px;');
        d.education.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.degree) + (e.field ? ' in ' + _esc(e.field) : '') + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.institution) + (e.grade ? ' | CGPA: ' + _esc(e.grade) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.experience.length > 0) {
        h += '<div class="tpl-exec-section">';
        h += _sectionTitle('Professional Experience', c, 'border-bottom:1px solid #ddd;padding-bottom:4px;margin-bottom:8px;');
        d.experience.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + (e.employmentType ? ' | ' + _esc(e.employmentType) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            if (e.achievements) h += '<div class="tpl-entry-desc"><strong>Achievements:</strong> ' + _esc(e.achievements) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.projects.length > 0) {
        h += '<div class="tpl-exec-section">';
        h += _sectionTitle('Projects', c, 'border-bottom:1px solid #ddd;padding-bottom:4px;margin-bottom:8px;');
        d.projects.forEach(p => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span>';
            if (p.startDate) h += '<span class="tpl-entry-date">' + _esc(p.startDate) + (p.endDate ? ' – ' + _esc(p.endDate) : '') + '</span>';
            h += '</div>';
            if (p.role) h += '<div class="tpl-entry-sub">' + _esc(p.role) + '</div>';
            if (p.description) h += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) h += '<div class="tpl-entry-tags">' + p.technologies.split(',').map(t => '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(t.trim()) + '</span>').join('') + '</div>';
            if (p.keyContributions) h += '<div class="tpl-entry-desc"><strong>Key Contributions:</strong> ' + _esc(p.keyContributions) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.skills.length > 0) {
        h += '<div class="tpl-exec-section">';
        h += _sectionTitle('Skills', c, 'border-bottom:1px solid #ddd;padding-bottom:4px;margin-bottom:8px;');
        h += '<div class="tpl-skills">';
        d.skills.forEach(s => { if (s.name) h += '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(s.name) + '</span>'; });
        h += '</div></div>';
    }
    if (d.certifications.length > 0) {
        h += '<div class="tpl-exec-section">';
        h += _sectionTitle('Certifications', c, 'border-bottom:1px solid #ddd;padding-bottom:4px;margin-bottom:8px;');
        d.certifications.forEach(cr => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(cr.name) + '</span>';
            if (cr.date) h += '<span class="tpl-entry-date">' + _esc(cr.date) + '</span></div>';
            if (cr.issuingOrganization) h += '<div class="tpl-entry-sub">' + _esc(cr.issuingOrganization) + (cr.credentialId ? ' | ID: ' + _esc(cr.credentialId) : '') + '</div>';
            if (cr.credentialUrl) h += '<div style="margin-top:2px;"><a href="' + _esc(cr.credentialUrl) + '" style="color:' + c + ';font-size:8.5pt;">View Credential</a></div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.achievements.length > 0) {
        h += '<div class="tpl-exec-section">';
        h += _sectionTitle('Achievements', c, 'border-bottom:1px solid #ddd;padding-bottom:4px;margin-bottom:8px;');
        d.achievements.forEach(a => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(a.title) + '</span>';
            if (a.date) h += '<span class="tpl-entry-date">' + _esc(a.date) + '</span></div>';
            if (a.description) h += '<div class="tpl-entry-desc">' + _esc(a.description) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.languages.length > 0) {
        h += '<div class="tpl-exec-section">';
        h += _sectionTitle('Languages', c, 'border-bottom:1px solid #ddd;padding-bottom:4px;margin-bottom:8px;');
        h += '<div class="tpl-lang-list">';
        d.languages.forEach(l => { if (l.name) h += '<span class="tpl-lang-item">' + _esc(l.name) + (l.proficiency ? ' (' + _esc(l.proficiency) + ')' : '') + '</span>'; });
        h += '</div></div>';
    }
    return h;
}

// ====================================================================
// TEMPLATE 2: CORPORATE PRO
// Two-column with sidebar, timeline experience
// ====================================================================
templates['corporate'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-corporate">';
    // Header
    h += '<div class="tpl-corp-header" style="background:' + c + ';color:#fff;padding:20px 24px;">';
    h += '<h1 style="font-family:' + ff + ';font-size:' + (fs+8) + 'pt;margin:0 0 4px;font-weight:700;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+1) + 'pt;opacity:0.9;">' + _esc(d.professionalTitle) + '</div>';
    h += '<div class="tpl-corp-contact" style="margin-top:10px;font-size:' + (fs-1) + 'pt;">';
    if (d.personalEmail) h += '<span>' + _esc(d.personalEmail) + '</span>';
    if (d.phone) h += '<span>' + _esc(d.phone) + '</span>';
    if (d.location) h += '<span>' + _esc(d.location) + '</span>';
    if (d.linkedin) h += '<a href="' + _esc(d.linkedin) + '" style="color:#fff;margin-right:12px;text-decoration:none;">LinkedIn</a>';
    if (d.github) h += '<a href="' + _esc(d.github) + '" style="color:#fff;margin-right:12px;text-decoration:none;">GitHub</a>';
    h += '</div></div>';
    // Body two-column
    h += '<div class="tpl-corp-body">';
    // Main column
    h += '<div class="tpl-corp-main">';
    if (d.summary) {
        h += '<div class="tpl-corp-section">';
        h += _sectionTitle('Summary', c);
        h += '<p style="font-size:' + (fs-0.5) + 'pt;color:#333;line-height:1.5;">' + _esc(d.summary) + '</p></div>';
    }
    if (d.experience.length > 0) {
        h += '<div class="tpl-corp-section">';
        h += _sectionTitle('Experience', c);
        d.experience.forEach(e => {
            h += '<div class="tpl-corp-exp"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            if (e.achievements) h += '<div class="tpl-entry-desc"><strong>Achievements:</strong> ' + _esc(e.achievements) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.projects.length > 0) {
        h += '<div class="tpl-corp-section">';
        h += _sectionTitle('Projects', c);
        d.projects.forEach(p => {
            h += '<div class="tpl-corp-exp"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span></div>';
            if (p.description) h += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) h += '<div class="tpl-entry-tags">' + p.technologies.split(',').map(t => '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(t.trim()) + '</span>').join('') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    h += '</div>';
    // Sidebar
    h += '<div class="tpl-corp-sidebar" style="border-left:1px solid #eee;padding-left:16px;">';
    if (d.education.length > 0) {
        h += '<div class="tpl-corp-section">';
        h += _sectionTitle('Education', c);
        d.education.forEach(e => {
            h += '<div class="tpl-corp-side-entry"><strong style="font-size:' + (fs-0.5) + 'pt;">' + _esc(e.degree) + '</strong>';
            if (e.field) h += '<div style="font-size:' + (fs-1.5) + 'pt;color:#555;">' + _esc(e.field) + '</div>';
            h += '<div style="font-size:' + (fs-1.5) + 'pt;color:#777;">' + _esc(e.institution) + '</div>';
            if (e.startDate) h += '<div style="font-size:' + (fs-2) + 'pt;color:#999;">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</div>';
            if (e.grade) h += '<div style="font-size:' + (fs-1.5) + 'pt;color:#555;">CGPA: ' + _esc(e.grade) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.skills.length > 0) {
        h += '<div class="tpl-corp-section">';
        h += _sectionTitle('Skills', c);
        h += '<div class="tpl-skills">';
        d.skills.forEach(s => { if (s.name) h += '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(s.name) + '</span>'; });
        h += '</div></div>';
    }
    if (d.certifications.length > 0) {
        h += '<div class="tpl-corp-section">';
        h += _sectionTitle('Certifications', c);
        d.certifications.forEach(cr => {
            h += '<div style="margin-bottom:6px;"><div style="font-size:' + (fs-0.5) + 'pt;font-weight:600;">' + _esc(cr.name) + '</div>';
            if (cr.issuingOrganization) h += '<div style="font-size:' + (fs-1.5) + 'pt;color:#555;">' + _esc(cr.issuingOrganization) + '</div>';
            if (cr.date) h += '<div style="font-size:' + (fs-2) + 'pt;color:#999;">' + _esc(cr.date) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.languages.length > 0) {
        h += '<div class="tpl-corp-section">';
        h += _sectionTitle('Languages', c);
        d.languages.forEach(l => { if (l.name) h += '<div style="font-size:' + (fs-1) + 'pt;margin-bottom:3px;">' + _esc(l.name) + (l.proficiency ? ' <span style="color:#999;">(' + _esc(l.proficiency) + ')</span>' : '') + '</div>'; });
        h += '</div>';
    }
    if (d.achievements.length > 0) {
        h += '<div class="tpl-corp-section">';
        h += _sectionTitle('Achievements', c);
        d.achievements.forEach(a => {
            h += '<div style="margin-bottom:6px;"><div style="font-size:' + (fs-0.5) + 'pt;font-weight:600;">' + _esc(a.title) + '</div>';
            if (a.description) h += '<div style="font-size:' + (fs-1.5) + 'pt;color:#555;">' + _esc(a.description) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    h += '</div></div></div>';
    return h;
}

// ====================================================================
// TEMPLATE 3: CLEARLINE
// Clean, modern header, balanced two-column, ATS-friendly
// ====================================================================
templates['clearline'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-clearline">';
    h += '<div class="tpl-cl-header" style="border-bottom:3px solid ' + c + ';">';
    h += '<h1 style="font-family:' + ff + ';color:' + c + ';font-size:' + (fs+9) + 'pt;margin:0;font-weight:800;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+1) + 'pt;color:#555;margin:4px 0 8px;">' + _esc(d.professionalTitle) + '</div>';
    h += _contactHtml(d, c);
    h += '</div>';
    if (d.summary) {
        h += '<div class="tpl-cl-section">';
        h += _sectionTitle('Profile', c, 'border-bottom:2px solid ' + c + ';padding-bottom:4px;');
        h += '<p style="font-size:' + (fs-0.5) + 'pt;color:#333;line-height:1.6;">' + _esc(d.summary) + '</p></div>';
    }
    h += _renderSectionsClearline(d, c, ff, fs);
    h += '</div>';
    return h;
};
function _renderSectionsClearline(d, c, ff, fs) {
    let h = '';
    if (d.experience.length > 0) {
        h += '<div class="tpl-cl-section">';
        h += _sectionTitle('Experience', c, 'border-bottom:2px solid ' + c + ';padding-bottom:4px;');
        d.experience.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + (e.employmentType ? ' | ' + _esc(e.employmentType) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            if (e.achievements) h += '<div class="tpl-entry-desc"><strong>Achievements:</strong> ' + _esc(e.achievements) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.education.length > 0) {
        h += '<div class="tpl-cl-section">';
        h += _sectionTitle('Education', c, 'border-bottom:2px solid ' + c + ';padding-bottom:4px;');
        d.education.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.degree) + (e.field ? ' in ' + _esc(e.field) : '') + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.institution) + (e.grade ? ' | CGPA: ' + _esc(e.grade) : '') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.skills.length > 0) {
        h += '<div class="tpl-cl-section">';
        h += _sectionTitle('Skills', c, 'border-bottom:2px solid ' + c + ';padding-bottom:4px;');
        h += '<div class="tpl-skills">';
        d.skills.forEach(s => { if (s.name) h += '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(s.name) + '</span>'; });
        h += '</div></div>';
    }
    if (d.projects.length > 0) {
        h += '<div class="tpl-cl-section">';
        h += _sectionTitle('Projects', c, 'border-bottom:2px solid ' + c + ';padding-bottom:4px;');
        d.projects.forEach(p => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span>';
            if (p.startDate) h += '<span class="tpl-entry-date">' + _esc(p.startDate) + '</span>';
            h += '</div>';
            if (p.description) h += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) h += '<div class="tpl-entry-tags">' + p.technologies.split(',').map(t => '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(t.trim()) + '</span>').join('') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.certifications.length > 0) {
        h += '<div class="tpl-cl-section">';
        h += _sectionTitle('Certifications', c, 'border-bottom:2px solid ' + c + ';padding-bottom:4px;');
        d.certifications.forEach(cr => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(cr.name) + '</span>';
            if (cr.date) h += '<span class="tpl-entry-date">' + _esc(cr.date) + '</span></div>';
            if (cr.issuingOrganization) h += '<div class="tpl-entry-sub">' + _esc(cr.issuingOrganization) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.achievements.length > 0) {
        h += '<div class="tpl-cl-section">';
        h += _sectionTitle('Achievements', c, 'border-bottom:2px solid ' + c + ';padding-bottom:4px;');
        d.achievements.forEach(a => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(a.title) + '</span>';
            if (a.date) h += '<span class="tpl-entry-date">' + _esc(a.date) + '</span></div>';
            if (a.description) h += '<div class="tpl-entry-desc">' + _esc(a.description) + '</div></div>';
        });
        h += '</div>';
    }
    if (d.languages.length > 0) {
        h += '<div class="tpl-cl-section">';
        h += _sectionTitle('Languages', c, 'border-bottom:2px solid ' + c + ';padding-bottom:4px;');
        h += '<div class="tpl-lang-list">';
        d.languages.forEach(l => { if (l.name) h += '<span class="tpl-lang-item">' + _esc(l.name) + (l.proficiency ? ' (' + _esc(l.proficiency) + ')' : '') + '</span>'; });
        h += '</div></div>';
    }
    return h;
}

// ====================================================================
// TEMPLATE 4: GOLD ACCENT
// Monochrome foundation + one elegant accent color, ATS-friendly
// ====================================================================
templates['gold'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-gold">';
    h += '<div class="tpl-gold-header">';
    h += '<div style="width:60px;height:60px;border-radius:50%;background:' + c + ';color:#fff;display:flex;align-items:center;justify-content:center;font-size:22pt;font-weight:700;flex-shrink:0;">' + _esc((d.firstName||'Y')[0] || '') + '</div>';
    h += '<div><h1 style="font-family:' + ff + ';color:#1a1a1a;font-size:' + (fs+8) + 'pt;margin:0;font-weight:700;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';font-weight:600;margin:2px 0 6px;">' + _esc(d.professionalTitle) + '</div>';
    h += _contactHtml(d, c);
    h += '</div></div>';
    h += '<div class="tpl-gold-divider" style="background:' + c + ';height:3px;margin:12px 0;"></div>';
    if (d.summary) {
        h += '<div class="tpl-gold-section">';
        h += _sectionTitle('Professional Summary', c, 'font-size:' + (fs+1) + 'pt;border-bottom:1px solid #ddd;padding-bottom:3px;margin-bottom:6px;');
        h += '<p style="font-size:' + (fs-0.5) + 'pt;color:#333;line-height:1.6;">' + _esc(d.summary) + '</p></div>';
    }
    h += _renderSectionsGold(d, c, ff, fs);
    h += '</div>';
    return h;
};
function _renderSectionsGold(d, c, ff, fs) {
    let h = '';
    if (d.experience.length > 0) {
        h += '<div class="tpl-gold-section">';
        h += _sectionTitle('Experience', c, 'font-size:' + (fs+1) + 'pt;border-bottom:1px solid #ddd;padding-bottom:3px;margin-bottom:6px;');
        d.experience.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            if (e.achievements) h += '<div class="tpl-entry-desc"><strong>Achievements:</strong> ' + _esc(e.achievements) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.education.length > 0) {
        h += '<div class="tpl-gold-section">';
        h += _sectionTitle('Education', c, 'font-size:' + (fs+1) + 'pt;border-bottom:1px solid #ddd;padding-bottom:3px;margin-bottom:6px;');
        d.education.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.degree) + (e.field ? ' in ' + _esc(e.field) : '') + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.institution) + (e.grade ? ' | CGPA: ' + _esc(e.grade) : '') + '</div></div>';
        });
        h += '</div>';
    }
    if (d.skills.length > 0) {
        h += '<div class="tpl-gold-section">';
        h += _sectionTitle('Skills', c, 'font-size:' + (fs+1) + 'pt;border-bottom:1px solid #ddd;padding-bottom:3px;margin-bottom:6px;');
        h += '<div class="tpl-skills">';
        d.skills.forEach(s => { if (s.name) h += '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(s.name) + '</span>'; });
        h += '</div></div>';
    }
    if (d.projects.length > 0) {
        h += '<div class="tpl-gold-section">';
        h += _sectionTitle('Projects', c, 'font-size:' + (fs+1) + 'pt;border-bottom:1px solid #ddd;padding-bottom:3px;margin-bottom:6px;');
        d.projects.forEach(p => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span>';
            if (p.startDate) h += '<span class="tpl-entry-date">' + _esc(p.startDate) + '</span>';
            h += '</div>';
            if (p.description) h += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) h += '<div class="tpl-entry-tags">' + p.technologies.split(',').map(t => '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(t.trim()) + '</span>').join('') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.certifications.length > 0) {
        h += '<div class="tpl-gold-section">';
        h += _sectionTitle('Certifications', c, 'font-size:' + (fs+1) + 'pt;border-bottom:1px solid #ddd;padding-bottom:3px;margin-bottom:6px;');
        d.certifications.forEach(cr => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(cr.name) + '</span>';
            if (cr.date) h += '<span class="tpl-entry-date">' + _esc(cr.date) + '</span></div>';
            if (cr.issuingOrganization) h += '<div class="tpl-entry-sub">' + _esc(cr.issuingOrganization) + '</div></div>';
        });
        h += '</div>';
    }
    if (d.languages.length > 0) {
        h += '<div class="tpl-gold-section">';
        h += _sectionTitle('Languages', c, 'font-size:' + (fs+1) + 'pt;border-bottom:1px solid #ddd;padding-bottom:3px;margin-bottom:6px;');
        h += '<div class="tpl-lang-list">';
        d.languages.forEach(l => { if (l.name) h += '<span class="tpl-lang-item">' + _esc(l.name) + (l.proficiency ? ' (' + _esc(l.proficiency) + ')' : '') + '</span>'; });
        h += '</div></div>';
    }
    if (d.achievements.length > 0) {
        h += '<div class="tpl-gold-section">';
        h += _sectionTitle('Achievements', c, 'font-size:' + (fs+1) + 'pt;border-bottom:1px solid #ddd;padding-bottom:3px;margin-bottom:6px;');
        d.achievements.forEach(a => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(a.title) + '</span>';
            if (a.date) h += '<span class="tpl-entry-date">' + _esc(a.date) + '</span></div>';
            if (a.description) h += '<div class="tpl-entry-desc">' + _esc(a.description) + '</div></div>';
        });
        h += '</div>';
    }
    return h;
}

// ====================================================================
// TEMPLATE 5: HARMONIZED
// Soft professional, balanced spacing, elegant separators
// ====================================================================
templates['harmonized'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-harmonized">';
    h += '<div class="tpl-har-header" style="text-align:center;padding-bottom:12px;border-bottom:1px solid #e0e0e0;">';
    h += '<h1 style="font-family:' + ff + ';color:' + c + ';font-size:' + (fs+9) + 'pt;margin:0;font-weight:600;letter-spacing:0.5px;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+1) + 'pt;color:#666;margin:4px 0 8px;">' + _esc(d.professionalTitle) + '</div>';
    h += '<div class="tpl-har-contact" style="display:flex;justify-content:center;gap:16px;font-size:' + (fs-1) + 'pt;color:#555;">';
    if (d.personalEmail) h += '<span>' + _esc(d.personalEmail) + '</span>';
    if (d.phone) h += '<span>' + _esc(d.phone) + '</span>';
    if (d.location) h += '<span>' + _esc(d.location) + '</span>';
    h += '</div>';
    if (d.linkedin || d.github || d.website) {
        h += '<div class="tpl-har-links" style="display:flex;justify-content:center;gap:12px;font-size:' + (fs-1.5) + 'pt;margin-top:4px;">';
        if (d.linkedin) h += '<a href="' + _esc(d.linkedin) + '" style="color:' + c + ';margin-right:10px;text-decoration:none;">LinkedIn</a>';
        if (d.github) h += '<a href="' + _esc(d.github) + '" style="color:' + c + ';margin-right:10px;text-decoration:none;">GitHub</a>';
        if (d.website) h += '<a href="' + _esc(d.website) + '" style="color:' + c + ';margin-right:10px;text-decoration:none;">Portfolio</a>';
        h += '</div>';
    }
    h += '</div>';
    h += _renderSectionsHarmonized(d, c, ff, fs);
    h += '</div>';
    return h;
};
function _renderSectionsHarmonized(d, c, ff, fs) {
    let h = '';
    if (d.summary) {
        h += '<div class="tpl-har-section" style="margin-top:12px;">';
        h += '<div style="text-align:center;font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:600;text-transform:uppercase;letter-spacing:1px;margin-bottom:6px;">Profile</div>';
        h += '<p style="font-size:' + (fs-0.5) + 'pt;color:#333;line-height:1.7;text-align:center;max-width:90%;">' + _esc(d.summary) + '</p></div>';
    }
    const sections = [
        { key: 'experience', title: 'Professional Experience', items: d.experience, render: (e) => {
            let r = '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            r += '<div class="tpl-entry-sub">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + '</div>';
            if (e.description) r += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            return r + '</div>';
        }},
        { key: 'education', title: 'Education', items: d.education, render: (e) => {
            return '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.degree) + (e.field ? ' in ' + _esc(e.field) : '') + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</span></div><div class="tpl-entry-sub">' + _esc(e.institution) + (e.grade ? ' | CGPA: ' + _esc(e.grade) : '') + '</div></div>';
        }},
        { key: 'projects', title: 'Projects', items: d.projects, render: (p) => {
            let r = '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span></div>';
            if (p.description) r += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) r += '<div class="tpl-entry-tags">' + p.technologies.split(',').map(t => '<span class="tpl-tag" style="background:' + c + '12;color:' + c + ';">' + _esc(t.trim()) + '</span>').join('') + '</div>';
            return r + '</div>';
        }},
        { key: 'skills', title: 'Skills', items: d.skills, render: () => {
            let r = '<div class="tpl-skills" style="text-align:center;">';
            d.skills.forEach(s => { if (s.name) r += '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(s.name) + '</span>'; });
            return r + '</div>';
        }},
        { key: 'certifications', title: 'Certifications', items: d.certifications, render: (cr) => {
            return '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(cr.name) + '</span>' + (cr.date ? '<span class="tpl-entry-date">' + _esc(cr.date) + '</span>' : '') + '</div><div class="tpl-entry-sub">' + _esc(cr.issuingOrganization) + '</div></div>';
        }},
        { key: 'achievements', title: 'Achievements', items: d.achievements, render: (a) => {
            return '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(a.title) + '</span>' + (a.date ? '<span class="tpl-entry-date">' + _esc(a.date) + '</span>' : '') + '</div>' + (a.description ? '<div class="tpl-entry-desc">' + _esc(a.description) + '</div>' : '') + '</div>';
        }},
        { key: 'languages', title: 'Languages', items: d.languages, render: () => {
            let r = '<div class="tpl-lang-list" style="text-align:center;">';
            d.languages.forEach(l => { if (l.name) r += '<span class="tpl-lang-item">' + _esc(l.name) + (l.proficiency ? ' (' + _esc(l.proficiency) + ')' : '') + '</span>'; });
            return r + '</div>';
        }}
    ];
    sections.forEach(sec => {
        if (sec.items && sec.items.length > 0) {
            h += '<div class="tpl-har-section" style="margin-top:12px;">';
            h += '<div style="text-align:center;font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:600;text-transform:uppercase;letter-spacing:1px;margin-bottom:6px;">' + sec.title + '</div>';
            h += '<div style="border-top:1px solid #e8e8e8;padding-top:6px;">';
            sec.items.forEach(item => { h += sec.render(item); });
            h += '</div></div>';
        }
    });
    return h;
}

// ====================================================================
// TEMPLATE 6: MODERN PROFILE
// Skills sidebar, modern, compact header
// ====================================================================
templates['modern-profile'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-modern-profile">';
    h += '<div class="tpl-mp-header" style="background:linear-gradient(135deg,' + c + ' 0%,' + c + 'cc 100%);color:#fff;padding:18px 20px;">';
    h += '<h1 style="font-family:' + ff + ';font-size:' + (fs+8) + 'pt;margin:0;font-weight:700;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+1) + 'pt;opacity:0.9;margin:2px 0 8px;">' + _esc(d.professionalTitle) + '</div>';
    h += '<div style="font-size:' + (fs-1) + 'pt;display:flex;flex-wrap:wrap;gap:12px;opacity:0.85;">';
    if (d.personalEmail) h += '<span>' + _esc(d.personalEmail) + '</span>';
    if (d.phone) h += '<span>' + _esc(d.phone) + '</span>';
    if (d.location) h += '<span>' + _esc(d.location) + '</span>';
    h += '</div></div>';
    h += '<div class="tpl-mp-body" style="display:flex;">';
    // Sidebar
    h += '<div class="tpl-mp-sidebar" style="width:35%;background:#f8f9fa;padding:14px;border-right:1px solid #eee;">';
    if (d.skills.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px;">Skills</div>';
        d.skills.forEach(s => {
            if (s.name) h += '<div style="font-size:' + (fs-1) + 'pt;padding:3px 0;border-bottom:1px solid #eee;">' + _esc(s.name) + (s.category ? ' <span style="color:#999;font-size:' + (fs-2) + 'pt;">' + _esc(s.category) + '</span>' : '') + '</div>';
        });
        h += '</div>';
    }
    if (d.education.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px;">Education</div>';
        d.education.forEach(e => {
            h += '<div style="margin-bottom:8px;"><div style="font-size:' + (fs-0.5) + 'pt;font-weight:600;">' + _esc(e.degree) + '</div>';
            if (e.field) h += '<div style="font-size:' + (fs-1.5) + 'pt;color:#555;">' + _esc(e.field) + '</div>';
            h += '<div style="font-size:' + (fs-1.5) + 'pt;color:#777;">' + _esc(e.institution) + '</div>';
            if (e.startDate) h += '<div style="font-size:' + (fs-2) + 'pt;color:#999;">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.languages.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px;">Languages</div>';
        d.languages.forEach(l => { if (l.name) h += '<div style="font-size:' + (fs-1) + 'pt;padding:2px 0;">' + _esc(l.name) + (l.proficiency ? ' <span style="color:#999;">(' + _esc(l.proficiency) + ')</span>' : '') + '</div>'; });
        h += '</div>';
    }
    if (d.certifications.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px;">Certifications</div>';
        d.certifications.forEach(cr => {
            h += '<div style="margin-bottom:6px;"><div style="font-size:' + (fs-1) + 'pt;font-weight:600;">' + _esc(cr.name) + '</div>';
            if (cr.date) h += '<div style="font-size:' + (fs-2) + 'pt;color:#999;">' + _esc(cr.date) + '</div></div>';
        });
        h += '</div>';
    }
    h += '</div>';
    // Main content
    h += '<div class="tpl-mp-main" style="flex:1;padding:14px;">';
    if (d.summary) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px;border-bottom:2px solid ' + c + ';padding-bottom:3px;">Profile</div>';
        h += '<p style="font-size:' + (fs-0.5) + 'pt;color:#333;line-height:1.6;">' + _esc(d.summary) + '</p></div>';
    }
    if (d.experience.length > 0) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px;border-bottom:2px solid ' + c + ';padding-bottom:3px;">Experience</div>';
        d.experience.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.projects.length > 0) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px;border-bottom:2px solid ' + c + ';padding-bottom:3px;">Projects</div>';
        d.projects.forEach(p => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span></div>';
            if (p.description) h += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) h += '<div class="tpl-entry-tags">' + p.technologies.split(',').map(t => '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';font-size:' + (fs-2) + 'pt;">' + _esc(t.trim()) + '</span>').join('') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.achievements.length > 0) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px;border-bottom:2px solid ' + c + ';padding-bottom:3px;">Achievements</div>';
        d.achievements.forEach(a => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(a.title) + '</span>';
            if (a.date) h += '<span class="tpl-entry-date">' + _esc(a.date) + '</span></div>';
            if (a.description) h += '<div class="tpl-entry-desc">' + _esc(a.description) + '</div></div>';
        });
        h += '</div>';
    }
    h += '</div></div></div>';
    return h;
}

// ====================================================================
// TEMPLATE 7: AUTHORITY
// Bold header, strong typography, high visual hierarchy
// ====================================================================
templates['authority'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-authority">';
    h += '<div class="tpl-auth-header" style="background:#1a1a2e;color:#fff;padding:20px 24px;">';
    h += '<h1 style="font-family:' + ff + ';font-size:' + (fs+10) + 'pt;margin:0;font-weight:800;text-transform:uppercase;letter-spacing:2px;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+2) + 'pt;color:' + c + ';font-weight:600;margin:4px 0 10px;text-transform:uppercase;letter-spacing:1px;">' + _esc(d.professionalTitle) + '</div>';
    h += '<div style="display:flex;flex-wrap:wrap;gap:14px;font-size:' + (fs-1) + 'pt;color:#ccc;">';
    if (d.personalEmail) h += '<span>' + _esc(d.personalEmail) + '</span>';
    if (d.phone) h += '<span>' + _esc(d.phone) + '</span>';
    if (d.location) h += '<span>' + _esc(d.location) + '</span>';
    if (d.linkedin) h += '<a href="' + _esc(d.linkedin) + '" style="color:' + c + ';margin-right:12px;text-decoration:none;">LinkedIn</a>';
    if (d.github) h += '<a href="' + _esc(d.github) + '" style="color:' + c + ';margin-right:12px;text-decoration:none;">GitHub</a>';
    h += '</div></div>';
    if (d.summary) {
        h += '<div class="tpl-auth-section" style="padding:12px 24px;background:#f8f9fa;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:4px;">Summary</div>';
        h += '<p style="font-size:' + (fs-0.5) + 'pt;color:#333;line-height:1.6;">' + _esc(d.summary) + '</p></div>';
    }
    h += '<div style="padding:12px 24px;">';
    h += _renderSectionsAuthority(d, c, ff, fs);
    h += '</div></div>';
    return h;
};
function _renderSectionsAuthority(d, c, ff, fs) {
    let h = '';
    if (d.experience.length > 0) {
        h += '<div class="tpl-auth-section" style="margin-bottom:12px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:6px;border-bottom:2px solid ' + c + ';padding-bottom:4px;">Experience</div>';
        d.experience.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            h += '<div class="tpl-entry-sub" style="font-weight:600;">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            if (e.achievements) h += '<div class="tpl-entry-desc"><strong>Key Achievements:</strong> ' + _esc(e.achievements) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.education.length > 0) {
        h += '<div class="tpl-auth-section" style="margin-bottom:12px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:6px;border-bottom:2px solid ' + c + ';padding-bottom:4px;">Education</div>';
        d.education.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.degree) + (e.field ? ' in ' + _esc(e.field) : '') + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.institution) + (e.grade ? ' | CGPA: ' + _esc(e.grade) : '') + '</div></div>';
        });
        h += '</div>';
    }
    if (d.skills.length > 0) {
        h += '<div class="tpl-auth-section" style="margin-bottom:12px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:6px;border-bottom:2px solid ' + c + ';padding-bottom:4px;">Skills</div>';
        h += '<div class="tpl-skills">';
        d.skills.forEach(s => { if (s.name) h += '<span class="tpl-tag" style="background:' + c + ';color:#fff;">' + _esc(s.name) + '</span>'; });
        h += '</div></div>';
    }
    if (d.projects.length > 0) {
        h += '<div class="tpl-auth-section" style="margin-bottom:12px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:6px;border-bottom:2px solid ' + c + ';padding-bottom:4px;">Projects</div>';
        d.projects.forEach(p => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span></div>';
            if (p.description) h += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) h += '<div class="tpl-entry-tags">' + p.technologies.split(',').map(t => '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(t.trim()) + '</span>').join('') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.certifications.length > 0) {
        h += '<div class="tpl-auth-section" style="margin-bottom:12px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:6px;border-bottom:2px solid ' + c + ';padding-bottom:4px;">Certifications</div>';
        d.certifications.forEach(cr => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(cr.name) + '</span>';
            if (cr.date) h += '<span class="tpl-entry-date">' + _esc(cr.date) + '</span></div>';
            if (cr.issuingOrganization) h += '<div class="tpl-entry-sub">' + _esc(cr.issuingOrganization) + '</div></div>';
        });
        h += '</div>';
    }
    if (d.languages.length > 0) {
        h += '<div class="tpl-auth-section" style="margin-bottom:12px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:6px;border-bottom:2px solid ' + c + ';padding-bottom:4px;">Languages</div>';
        h += '<div class="tpl-lang-list">';
        d.languages.forEach(l => { if (l.name) h += '<span class="tpl-lang-item">' + _esc(l.name) + (l.proficiency ? ' (' + _esc(l.proficiency) + ')' : '') + '</span>'; });
        h += '</div></div>';
    }
    if (d.achievements.length > 0) {
        h += '<div class="tpl-auth-section" style="margin-bottom:12px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:6px;border-bottom:2px solid ' + c + ';padding-bottom:4px;">Achievements</div>';
        d.achievements.forEach(a => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(a.title) + '</span>';
            if (a.date) h += '<span class="tpl-entry-date">' + _esc(a.date) + '</span></div>';
            if (a.description) h += '<div class="tpl-entry-desc">' + _esc(a.description) + '</div></div>';
        });
        h += '</div>';
    }
    return h;
}

// ====================================================================
// TEMPLATE 8: BALANCED
// Traditional + modern, two-column, excellent whitespace
// ====================================================================
templates['balanced'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-balanced">';
    h += '<div class="tpl-bal-header" style="padding:16px 0;border-bottom:4px solid ' + c + ';">';
    h += '<h1 style="font-family:' + ff + ';color:#222;font-size:' + (fs+9) + 'pt;margin:0;font-weight:700;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+1) + 'pt;color:' + c + ';margin:2px 0 6px;">' + _esc(d.professionalTitle) + '</div>';
    h += '<div style="font-size:' + (fs-1) + 'pt;color:#555;display:flex;flex-wrap:wrap;gap:12px;">';
    if (d.personalEmail) h += '<span>' + _esc(d.personalEmail) + '</span>';
    if (d.phone) h += '<span>' + _esc(d.phone) + '</span>';
    if (d.location) h += '<span>' + _esc(d.location) + '</span>';
    if (d.linkedin) h += '<a href="' + _esc(d.linkedin) + '" style="color:' + c + ';margin-right:12px;text-decoration:none;">LinkedIn</a>';
    if (d.github) h += '<a href="' + _esc(d.github) + '" style="color:' + c + ';margin-right:12px;text-decoration:none;">GitHub</a>';
    h += '</div></div>';
    h += '<div class="tpl-bal-body" style="display:flex;gap:16px;margin-top:12px;">';
    // Main
    h += '<div class="tpl-bal-main" style="flex:2;">';
    if (d.summary) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:4px;">About Me</div>';
        h += '<p style="font-size:' + (fs-0.5) + 'pt;color:#333;line-height:1.6;">' + _esc(d.summary) + '</p></div>';
    }
    if (d.experience.length > 0) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:4px;">Experience</div>';
        d.experience.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.projects.length > 0) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:4px;">Projects</div>';
        d.projects.forEach(p => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span></div>';
            if (p.description) h += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) h += '<div class="tpl-entry-tags">' + p.technologies.split(',').map(t => '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(t.trim()) + '</span>').join('') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    h += '</div>';
    // Sidebar
    h += '<div class="tpl-bal-sidebar" style="flex:1;padding-left:16px;border-left:1px solid #e0e0e0;">';
    if (d.education.length > 0) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:4px;">Education</div>';
        d.education.forEach(e => {
            h += '<div style="margin-bottom:8px;"><div style="font-size:' + (fs-0.5) + 'pt;font-weight:600;">' + _esc(e.degree) + '</div>';
            if (e.field) h += '<div style="font-size:' + (fs-1.5) + 'pt;color:#555;">' + _esc(e.field) + '</div>';
            h += '<div style="font-size:' + (fs-1.5) + 'pt;color:#777;">' + _esc(e.institution) + '</div>';
            if (e.startDate) h += '<div style="font-size:' + (fs-2) + 'pt;color:#999;">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.skills.length > 0) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:4px;">Skills</div>';
        h += '<div class="tpl-skills">';
        d.skills.forEach(s => { if (s.name) h += '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(s.name) + '</span>'; });
        h += '</div></div>';
    }
    if (d.certifications.length > 0) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:4px;">Certifications</div>';
        d.certifications.forEach(cr => {
            h += '<div style="margin-bottom:6px;"><div style="font-size:' + (fs-1) + 'pt;font-weight:600;">' + _esc(cr.name) + '</div>';
            if (cr.date) h += '<div style="font-size:' + (fs-2) + 'pt;color:#999;">' + _esc(cr.date) + '</div></div>';
        });
        h += '</div>';
    }
    if (d.languages.length > 0) {
        h += '<div style="margin-bottom:12px;"><div style="font-size:' + (fs+0.5) + 'pt;color:' + c + ';font-weight:700;text-transform:uppercase;margin-bottom:4px;">Languages</div>';
        d.languages.forEach(l => { if (l.name) h += '<div style="font-size:' + (fs-1) + 'pt;padding:2px 0;">' + _esc(l.name) + (l.proficiency ? ' <span style="color:#999;">(' + _esc(l.proficiency) + ')</span>' : '') + '</div>'; });
        h += '</div>';
    }
    h += '</div></div></div>';
    return h;
}

// ====================================================================
// TEMPLATE 9: STATEMENT
// Minimalist, bold header, one subtle accent
// ====================================================================
templates['statement'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-statement">';
    h += '<div class="tpl-stmt-header" style="padding:16px 0;">';
    h += '<h1 style="font-family:' + ff + ';color:#111;font-size:' + (fs+11) + 'pt;margin:0;font-weight:800;line-height:1.1;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+2) + 'pt;color:' + c + ';font-weight:500;margin:4px 0 8px;">' + _esc(d.professionalTitle) + '</div>';
    h += '<div style="font-size:' + (fs-1) + 'pt;color:#666;display:flex;flex-wrap:wrap;gap:14px;">';
    if (d.personalEmail) h += '<span>' + _esc(d.personalEmail) + '</span>';
    if (d.phone) h += '<span>' + _esc(d.phone) + '</span>';
    if (d.location) h += '<span>' + _esc(d.location) + '</span>';
    if (d.linkedin) h += '<a href="' + _esc(d.linkedin) + '" style="color:' + c + ';margin-right:12px;text-decoration:none;">LinkedIn</a>';
    if (d.github) h += '<a href="' + _esc(d.github) + '" style="color:' + c + ';margin-right:12px;text-decoration:none;">GitHub</a>';
    h += '</div></div>';
    h += '<div style="width:40px;height:3px;background:' + c + ';margin:8px 0 12px;"></div>';
    if (d.summary) {
        h += '<div style="margin-bottom:14px;"><p style="font-size:' + (fs) + 'pt;color:#333;line-height:1.7;font-style:italic;">' + _esc(d.summary) + '</p></div>';
    }
    h += _renderSectionsStatement(d, c, ff, fs);
    h += '</div>';
    return h;
};
function _renderSectionsStatement(d, c, ff, fs) {
    let h = '';
    if (d.experience.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;margin-bottom:6px;">Experience</div>';
        d.experience.forEach(e => {
            h += '<div class="tpl-entry" style="border-left:3px solid ' + c + ';padding-left:10px;margin-bottom:10px;">';
            h += '<div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            if (e.achievements) h += '<div class="tpl-entry-desc"><strong>Achievements:</strong> ' + _esc(e.achievements) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.education.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;margin-bottom:6px;">Education</div>';
        d.education.forEach(e => {
            h += '<div class="tpl-entry" style="border-left:3px solid ' + c + ';padding-left:10px;margin-bottom:8px;">';
            h += '<div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.degree) + (e.field ? ' in ' + _esc(e.field) : '') + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.institution) + (e.grade ? ' | CGPA: ' + _esc(e.grade) : '') + '</div></div>';
        });
        h += '</div>';
    }
    if (d.skills.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;margin-bottom:6px;">Skills</div>';
        h += '<div class="tpl-skills">';
        d.skills.forEach(s => { if (s.name) h += '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(s.name) + '</span>'; });
        h += '</div></div>';
    }
    if (d.projects.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;margin-bottom:6px;">Projects</div>';
        d.projects.forEach(p => {
            h += '<div class="tpl-entry" style="border-left:3px solid ' + c + ';padding-left:10px;margin-bottom:8px;">';
            h += '<div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span></div>';
            if (p.description) h += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) h += '<div class="tpl-entry-tags">' + p.technologies.split(',').map(t => '<span class="tpl-tag" style="background:' + c + '15;color:' + c + ';">' + _esc(t.trim()) + '</span>').join('') + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.certifications.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;margin-bottom:6px;">Certifications</div>';
        d.certifications.forEach(cr => {
            h += '<div class="tpl-entry" style="border-left:3px solid ' + c + ';padding-left:10px;margin-bottom:6px;">';
            h += '<div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(cr.name) + '</span>';
            if (cr.date) h += '<span class="tpl-entry-date">' + _esc(cr.date) + '</span></div>';
            if (cr.issuingOrganization) h += '<div class="tpl-entry-sub">' + _esc(cr.issuingOrganization) + '</div></div>';
        });
        h += '</div>';
    }
    if (d.languages.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;margin-bottom:6px;">Languages</div>';
        h += '<div class="tpl-lang-list">';
        d.languages.forEach(l => { if (l.name) h += '<span class="tpl-lang-item">' + _esc(l.name) + (l.proficiency ? ' (' + _esc(l.proficiency) + ')' : '') + '</span>'; });
        h += '</div></div>';
    }
    if (d.achievements.length > 0) {
        h += '<div style="margin-bottom:14px;"><div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;margin-bottom:6px;">Achievements</div>';
        d.achievements.forEach(a => {
            h += '<div class="tpl-entry" style="border-left:3px solid ' + c + ';padding-left:10px;margin-bottom:6px;">';
            h += '<div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(a.title) + '</span>';
            if (a.date) h += '<span class="tpl-entry-date">' + _esc(a.date) + '</span></div>';
            if (a.description) h += '<div class="tpl-entry-desc">' + _esc(a.description) + '</div></div>';
        });
        h += '</div>';
    }
    return h;
}

// ====================================================================
// TEMPLATE 10: CLASSIC PRO
// Traditional single-column, strong headings, ATS-friendly
// ====================================================================
templates['classic'] = function(d, c, ff, fs) {
    const name = ((d.firstName||'') + ' ' + (d.lastName||'')).trim() || 'Your Name';
    let h = '<div class="tpl-classic">';
    h += '<div class="tpl-cp-header" style="text-align:center;padding-bottom:10px;border-bottom:2px solid #333;">';
    h += '<h1 style="font-family:' + ff + ';color:#111;font-size:' + (fs+10) + 'pt;margin:0;text-transform:uppercase;letter-spacing:1px;">' + _esc(name) + '</h1>';
    if (d.professionalTitle) h += '<div style="font-size:' + (fs+1) + 'pt;color:#444;margin:4px 0 8px;">' + _esc(d.professionalTitle) + '</div>';
    h += '<div style="font-size:' + (fs-1) + 'pt;color:#555;">';
    const parts = [];
    if (d.personalEmail) parts.push(_esc(d.personalEmail));
    if (d.phone) parts.push(_esc(d.phone));
    if (d.location) parts.push(_esc(d.location));
    if (d.linkedin) parts.push('LinkedIn: ' + _esc(d.linkedin));
    if (d.github) parts.push('GitHub: ' + _esc(d.github));
    h += parts.join(' | ');
    h += '</div></div>';
    h += _renderSectionsClassic(d, c, ff, fs);
    h += '</div>';
    return h;
};
function _renderSectionsClassic(d, c, ff, fs) {
    let h = '';
    if (d.summary) {
        h += '<div class="tpl-cp-section" style="margin-top:10px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;text-transform:uppercase;border-bottom:1px solid #999;padding-bottom:3px;margin-bottom:6px;">Professional Summary</div>';
        h += '<p style="font-size:' + (fs-0.5) + 'pt;color:#333;line-height:1.6;">' + _esc(d.summary) + '</p></div>';
    }
    if (d.experience.length > 0) {
        h += '<div class="tpl-cp-section" style="margin-top:10px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;text-transform:uppercase;border-bottom:1px solid #999;padding-bottom:3px;margin-bottom:6px;">Professional Experience</div>';
        d.experience.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.jobTitle) + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.currentlyWorking ? ' – Present' : (e.endDate ? ' – ' + _esc(e.endDate) : '')) + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.company) + (e.location ? ', ' + _esc(e.location) : '') + '</div>';
            if (e.description) h += '<div class="tpl-entry-desc">' + _esc(e.description) + '</div>';
            if (e.achievements) h += '<div class="tpl-entry-desc"><strong>Achievements:</strong> ' + _esc(e.achievements) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.education.length > 0) {
        h += '<div class="tpl-cp-section" style="margin-top:10px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;text-transform:uppercase;border-bottom:1px solid #999;padding-bottom:3px;margin-bottom:6px;">Education</div>';
        d.education.forEach(e => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(e.degree) + (e.field ? ' in ' + _esc(e.field) : '') + '</span><span class="tpl-entry-date">' + _esc(e.startDate) + (e.endDate ? ' – ' + _esc(e.endDate) : '') + '</span></div>';
            h += '<div class="tpl-entry-sub">' + _esc(e.institution) + (e.grade ? ' | CGPA: ' + _esc(e.grade) : '') + '</div></div>';
        });
        h += '</div>';
    }
    if (d.skills.length > 0) {
        h += '<div class="tpl-cp-section" style="margin-top:10px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;text-transform:uppercase;border-bottom:1px solid #999;padding-bottom:3px;margin-bottom:6px;">Skills</div>';
        h += '<div style="font-size:' + (fs-0.5) + 'pt;color:#333;">';
        d.skills.forEach(s => { if (s.name) h += _esc(s.name) + (s.category ? ' (' + _esc(s.category) + ')' : '') + ' &bull; '; });
        h = h.replace(/ &bull; $/, '');
        h += '</div></div>';
    }
    if (d.projects.length > 0) {
        h += '<div class="tpl-cp-section" style="margin-top:10px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;text-transform:uppercase;border-bottom:1px solid #999;padding-bottom:3px;margin-bottom:6px;">Projects</div>';
        d.projects.forEach(p => {
            h += '<div class="tpl-entry"><div class="tpl-entry-row"><span class="tpl-entry-title">' + _esc(p.name) + '</span></div>';
            if (p.description) h += '<div class="tpl-entry-desc">' + _esc(p.description) + '</div>';
            if (p.technologies) h += '<div style="font-size:' + (fs-1) + 'pt;color:#555;margin-top:2px;"><strong>Technologies:</strong> ' + _esc(p.technologies) + '</div>';
            h += '</div>';
        });
        h += '</div>';
    }
    if (d.certifications.length > 0) {
        h += '<div class="tpl-cp-section" style="margin-top:10px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;text-transform:uppercase;border-bottom:1px solid #999;padding-bottom:3px;margin-bottom:6px;">Certifications</div>';
        d.certifications.forEach(cr => {
            h += '<div style="font-size:' + (fs-0.5) + 'pt;margin-bottom:4px;">' + _esc(cr.name) + (cr.issuingOrganization ? ' — ' + _esc(cr.issuingOrganization) : '') + (cr.date ? ' (' + _esc(cr.date) + ')' : '') + '</div>';
        });
        h += '</div>';
    }
    if (d.achievements.length > 0) {
        h += '<div class="tpl-cp-section" style="margin-top:10px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;text-transform:uppercase;border-bottom:1px solid #999;padding-bottom:3px;margin-bottom:6px;">Achievements</div>';
        d.achievements.forEach(a => {
            h += '<div style="font-size:' + (fs-0.5) + 'pt;margin-bottom:4px;">&bull; ' + _esc(a.title) + (a.description ? ' — ' + _esc(a.description) : '') + (a.date ? ' (' + _esc(a.date) + ')' : '') + '</div>';
        });
        h += '</div>';
    }
    if (d.languages.length > 0) {
        h += '<div class="tpl-cp-section" style="margin-top:10px;">';
        h += '<div style="font-size:' + (fs+1) + 'pt;color:#111;font-weight:700;text-transform:uppercase;border-bottom:1px solid #999;padding-bottom:3px;margin-bottom:6px;">Languages</div>';
        h += '<div style="font-size:' + (fs-0.5) + 'pt;color:#333;">';
        d.languages.forEach(l => { if (l.name) h += _esc(l.name) + (l.proficiency ? ' — ' + _esc(l.proficiency) : '') + ' &bull; '; });
        h = h.replace(/ &bull; $/, '');
        h += '</div></div>';
    }
    return h;
}

// ====================================================================
// BACKWARD-COMPATIBLE ALIASES
// Old saved resumes may reference template IDs that no longer exist.
// Map legacy IDs to current templates.
// ====================================================================
templates['professional'] = templates['executive'];
templates['modern'] = templates['modern-profile'];
templates['student'] = templates['clearline'];
templates['developer'] = templates['balanced'];
templates['ats'] = templates['classic'];
templates['creative'] = templates['statement'];

// ====================================================================
// TEMPLATE DEFINITIONS (for gallery)
// ====================================================================
const templateDefinitions = [
    { id: 'executive', name: 'Executive', category: 'Professional', ats: true, desc: 'Elegant single-column with strong typography and thin dividers', color: '#1e293b' },
    { id: 'corporate', name: 'Corporate Pro', category: 'Corporate', ats: false, desc: 'Professional two-column with colored header and sidebar', color: '#2563eb' },
    { id: 'clearline', name: 'ClearLine', category: 'Professional', ats: true, desc: 'Clean modern layout with balanced hierarchy', color: '#0891b2' },
    { id: 'gold', name: 'Gold Accent', category: 'Professional', ats: true, desc: 'Monochrome with elegant accent color and avatar', color: '#d97706' },
    { id: 'harmonized', name: 'Harmonized', category: 'Professional', ats: false, desc: 'Soft professional with centered headers and balanced spacing', color: '#7c3aed' },
    { id: 'modern-profile', name: 'Modern Profile', category: 'Modern', ats: false, desc: 'Skills sidebar with gradient header and compact layout', color: '#059669' },
    { id: 'authority', name: 'Authority', category: 'Corporate', ats: false, desc: 'Bold dark header with strong visual hierarchy', color: '#dc2626' },
    { id: 'balanced', name: 'Balanced', category: 'Professional', ats: false, desc: 'Traditional two-column with excellent whitespace', color: '#2563eb' },
    { id: 'statement', name: 'Statement', category: 'Creative', ats: false, desc: 'Minimalist with bold typography and left-accent entries', color: '#111827' },
    { id: 'classic', name: 'Classic Pro', category: 'Professional', ats: true, desc: 'Traditional single-column, maximum ATS compatibility', color: '#374151' }
];
