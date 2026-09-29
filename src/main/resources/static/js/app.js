let currentUsername="";
let allRepositories=[];
let filteredRepositories=[];
let visibleRepositoryCount=10;

async function analyzeGitHub(){
    const analyzeButton=document.querySelector(".search-box button");
    const resultPanel=document.getElementById("result");
    const username=document.getElementById("username").value.trim();
    const profileResult=document.getElementById("profileResult");
    const repositoriesResult=document.getElementById("repositoriesResult");

    analyzeButton.disabled=true;
    analyzeButton.textContent="⏳ Analyzing...";

    resultPanel.innerHTML=`
        <div id="profileResult">
            <div class="loading-state">
                <div class="loading-spinner"></div>
                <h3>Analyzing GitHub profile...</h3>
                <p>Fetching repositories and building developer insights.</p>
            </div>
        </div>
        <div id="repositoriesResult"></div>
    `;

    const updatedProfileResult=document.getElementById("profileResult");
    const updatedRepositoriesResult=document.getElementById("repositoriesResult");

    if(!username){
        analyzeButton.disabled=false;
        analyzeButton.textContent="Analyze";
        updatedProfileResult.innerHTML=`<div class="error-message">Please enter a GitHub username.</div>`;
        updatedRepositoriesResult.innerHTML="";
        return;
    }

    currentUsername=username;
    visibleRepositoryCount=10;

    try{
        const [profileResponse,repositoriesResponse,analyticsResponse,summaryResponse]=await Promise.all([
            fetch(`/github/${username}`),
            fetch(`/github/${username}/repositories`),
            fetch(`/github/${username}/analytics`),
            fetch(`/github/${username}/summary`)
        ]);

        if(!profileResponse.ok) throw new Error("GitHub user not found.");
        if(!repositoriesResponse.ok) throw new Error("Unable to load repositories.");
        if(!analyticsResponse.ok) throw new Error("Unable to load analytics.");
        if(!summaryResponse.ok) throw new Error("Unable to load developer summary.");

        const profile=await profileResponse.json();
        allRepositories=await repositoriesResponse.json();
        filteredRepositories=[...allRepositories];

        const analytics=await analyticsResponse.json();
        const summary=await summaryResponse.json();

        displayProfile(profile);
        displayRepositories(filteredRepositories);
        displayAnalytics(analytics,summary);

        analyzeButton.disabled=false;
        analyzeButton.textContent="Analyze";
    }catch(error){
        analyzeButton.disabled=false;
        analyzeButton.textContent="Analyze";
        updatedProfileResult.innerHTML=`<div class="error-message">${error.message}</div>`;
        updatedRepositoriesResult.innerHTML="";
    }
}

function displayProfile(profile){
    document.getElementById("profileResult").innerHTML=`
        <div class="profile-card">
            <img src="${profile.avatarUrl}" alt="${profile.login}" class="profile-avatar">
            <div class="profile-info">
                <h2>${profile.name||profile.login}</h2>
                <p>@${profile.login}</p>
                ${profile.bio?`<p class="profile-bio">${profile.bio}</p>`:""}
                <div class="profile-stats">
                    <span>📦 ${profile.publicRepos} Repos</span>
                    <span>👥 ${profile.followers} Followers</span>
                    <span>➕ ${profile.following} Following</span>
                </div>
                <a href="${profile.htmlUrl}" target="_blank" class="github-link">View GitHub Profile →</a>
            </div>
        </div>
    `;
}

function displayRepositories(repositories){
    const repositoriesResult=document.getElementById("repositoriesResult");

    if(!repositories.length){
        repositoriesResult.innerHTML=`
            <div class="empty-result">
                <h3>No repositories found</h3>
                <p>Try changing your search or language filter.</p>
            </div>
        `;
        return;
    }

    const visibleRepositories=repositories.slice(0,visibleRepositoryCount);
    const languages=[...new Set(allRepositories.map(repo=>repo.language).filter(Boolean))].sort();

    let html=`
        <div id="repositoriesSection" class="repository-section">
            <div style="margin-bottom:18px">
                <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:15px;margin-bottom:15px">
                    <div>
                        <h3>Repositories</h3>
                        <p style="margin:5px 0 0;color:#777;font-size:12px">
                            Showing ${visibleRepositories.length} of ${repositories.length} matching repositories
                        </p>
                    </div>
                    <span style="padding:7px 10px;border-radius:20px;background:#eee9ff;color:#5b3fd1;font-size:11px;font-weight:bold;white-space:nowrap">
                        ${repositories.length} Found
                    </span>
                </div>

                <div style="display:flex;gap:10px;align-items:center">
                    <input
                        id="repositorySearch"
                        type="text"
                        placeholder="🔍 Search repository..."
                        value="${escapeHtml(document.getElementById("repositorySearch")?.value||"")}"
                        oninput="filterRepositories()"
                        style="flex:1;padding:11px 13px;border:1px solid #dddddd;border-radius:10px;outline:none;font-size:12px"
                    >

                    <select
                        id="languageFilter"
                        onchange="filterRepositories()"
                        style="padding:11px 13px;border:1px solid #dddddd;border-radius:10px;background:white;color:#17172b;outline:none;font-size:12px"
                    >
                        <option value="">All Languages</option>
                        ${languages.map(language=>`
                            <option value="${escapeHtml(language)}">${escapeHtml(language)}</option>
                        `).join("")}
                    </select>

                    <button
                        onclick="clearRepositoryFilters()"
                        style="padding:11px 14px;border:1px solid #dddddd;border-radius:10px;background:white;color:#666;font-size:12px;cursor:pointer"
                    >
                        Clear
                    </button>
                </div>
            </div>

            <div class="repository-list">
    `;

    visibleRepositories.forEach(repo=>{
        html+=`
            <div class="repository-card">
                <div class="repository-main">
                    <h4><a href="${repo.htmlUrl}" target="_blank">${escapeHtml(repo.name)}</a></h4>
                    <p>${escapeHtml(repo.description||"No description available.")}</p>
                    <div class="repository-meta">
                        <span>💻 ${escapeHtml(repo.language||"Unknown")}</span>
                        <span>⭐ ${repo.stars}</span>
                        <span>🍴 ${repo.forks}</span>
                        <span>🕒 ${formatGitHubDate(repo.pushedAt)}</span>
                    </div>
                </div>

                <div class="repository-quality-mini">
                    <span>Quality</span>
                    <strong>${repo.qualityScore}/100 · ${repo.qualityLevel}</strong>
                </div>
            </div>
        `;
    });

    html+=`</div>`;

    if(visibleRepositoryCount<repositories.length){
        html+=`
            <div style="text-align:center;margin-top:18px">
                <button
                    onclick="loadMoreRepositories()"
                    style="padding:11px 20px;border:none;border-radius:10px;background:#6c4cff;color:white;font-size:12px;font-weight:bold;cursor:pointer"
                >
                    Load More Repositories
                </button>
            </div>
        `;
    }else{
        html+=`
            <div style="text-align:center;margin-top:18px;color:#777;font-size:11px">
                ${repositories.length>0?"All matching repositories are displayed.":""}
            </div>
        `;
    }

    html+=`</div>`;
    repositoriesResult.innerHTML=html;

    const searchInput=document.getElementById("repositorySearch");
    const languageSelect=document.getElementById("languageFilter");

    if(searchInput){
        searchInput.addEventListener("keydown",event=>{
            if(event.key==="Enter") filterRepositories();
        });
    }

    if(languageSelect&&window.selectedLanguageFilter){
        languageSelect.value=window.selectedLanguageFilter;
    }
}

function filterRepositories(){
    const search=document.getElementById("repositorySearch")?.value.trim().toLowerCase()||"";
    const language=document.getElementById("languageFilter")?.value||"";

    window.selectedLanguageFilter=language;
    visibleRepositoryCount=10;

    filteredRepositories=allRepositories.filter(repo=>{
        const name=(repo.name||"").toLowerCase();
        const description=(repo.description||"").toLowerCase();
        const repoLanguage=repo.language||"";

        const matchesSearch=
            !search||
            name.includes(search)||
            description.includes(search);

        const matchesLanguage=
            !language||
            repoLanguage===language;

        return matchesSearch&&matchesLanguage;
    });

    displayRepositories(filteredRepositories);
}

function clearRepositoryFilters(){
    window.selectedLanguageFilter="";
    visibleRepositoryCount=10;
    filteredRepositories=[...allRepositories];
    displayRepositories(filteredRepositories);
}

function loadMoreRepositories(){
    visibleRepositoryCount+=10;
    displayRepositories(filteredRepositories);
}

function escapeHtml(value){
    return String(value)
        .replace(/&/g,"&amp;")
        .replace(/</g,"&lt;")
        .replace(/>/g,"&gt;")
        .replace(/"/g,"&quot;")
        .replace(/'/g,"&#039;");
}

function displayAnalytics(analytics,summary){
    const container=document.getElementById("repositoriesResult");

    container.insertAdjacentHTML("afterbegin",`
        ${createDashboardNavigation()}
        ${createProfileSummary(summary)}
        ${createGitHealthScore(analytics)}
        ${createActivityInsights(analytics)}
        ${createTechnologyProfile(analytics)}
        ${createRepositoryQuality(analytics)}
        ${createSkillEvidence(analytics)}
        ${createTopRepositories(analytics)}
        ${createTargetRoleSection()}
    `);

    loadTargetRoles();
}

function createDashboardNavigation(){
    return `
        <div class="dashboard-navigation">
            <div class="dashboard-navigation-inner">
                <a href="#overviewSection" class="dashboard-nav-active">Overview</a>
                <a href="#technologySection">Technology</a>
                <a href="#qualitySection">Quality</a>
                <a href="#skillSection">Skills</a>
                <a href="#roleSection">Role Analysis</a>
                <a href="#repositoriesSection">Repositories</a>
            </div>
        </div>
    `;
}

function createProfileSummary(summary){
    return `
        <div id="overviewSection" style="margin-bottom:30px;padding:20px;border-radius:16px;background:#fafaff;border:1px solid #eeeeee;text-align:left">
            <div style="display:flex;align-items:flex-start;justify-content:space-between;gap:15px;margin-bottom:18px">
                <div>
                    <h3 style="margin:0 0 6px;font-size:18px;color:#17172b">Developer Summary</h3>
                    <p style="margin:0;color:#777;font-size:12px;line-height:1.5">A quick snapshot of this developer's GitHub profile.</p>
                </div>
                <span style="padding:7px 10px;border-radius:20px;background:#eee9ff;color:#5b3fd1;font-size:11px;font-weight:bold;white-space:nowrap">${summary.activityLevel}</span>
            </div>

            <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:10px">
                <div style="padding:14px;border-radius:10px;background:white;border:1px solid #eeeeee">
                    <small style="color:#777;font-size:10px">GITHEALTH SCORE</small>
                    <strong style="display:block;margin-top:5px;color:#5b3fd1;font-size:22px">${summary.gitHealthScore}/100</strong>
                </div>

                <div style="padding:14px;border-radius:10px;background:white;border:1px solid #eeeeee">
                    <small style="color:#777;font-size:10px">REPOSITORIES</small>
                    <strong style="display:block;margin-top:5px;color:#17172b;font-size:20px">${summary.totalRepositories}</strong>
                </div>

                <div style="padding:14px;border-radius:10px;background:white;border:1px solid #eeeeee">
                    <small style="color:#777;font-size:10px">PRIMARY LANGUAGE</small>
                    <strong style="display:block;margin-top:5px;color:#17172b;font-size:16px">${summary.primaryLanguage}</strong>
                </div>

                <div style="padding:14px;border-radius:10px;background:white;border:1px solid #eeeeee">
                    <small style="color:#777;font-size:10px">TOTAL STARS</small>
                    <strong style="display:block;margin-top:5px;color:#17172b;font-size:20px">⭐ ${summary.totalStars}</strong>
                </div>

                <div style="padding:14px;border-radius:10px;background:white;border:1px solid #eeeeee">
                    <small style="color:#777;font-size:10px">TOTAL FORKS</small>
                    <strong style="display:block;margin-top:5px;color:#17172b;font-size:20px">🍴 ${summary.totalForks}</strong>
                </div>

                <div style="padding:14px;border-radius:10px;background:white;border:1px solid #eeeeee">
                    <small style="color:#777;font-size:10px">PROFILE FOCUS</small>
                    <strong style="display:block;margin-top:5px;color:#17172b;font-size:14px">${summary.profileFocus}</strong>
                </div>
            </div>
        </div>
    `;
}

function createGitHealthScore(data){
    const score=data.gitHealthScore||0;

    return `
        <div class="analytics-section">
            <div class="analytics-header">
                <div>
                    <h3>GitHealth Score</h3>
                    <p>Overall developer activity and repository strength.</p>
                </div>
                <div class="score-circle"><strong>${score}</strong><span>/100</span></div>
            </div>

            ${createScoreBreakdown(data)}
        </div>
    `;
}

function createScoreBreakdown(data){
    return `
        <div class="score-breakdown">
            ${createScoreItem("Repositories",data.repositoryScore)}
            ${createScoreItem("Stars",data.starScore)}
            ${createScoreItem("Forks",data.forkScore)}
            ${createScoreItem("Languages",data.languageScore)}
            ${createScoreItem("Popularity",data.popularityScore)}
        </div>
    `;
}

function createScoreItem(label,score){
    return `<div class="score-item"><span>${label}</span><strong>${score}</strong></div>`;
}

function createActivityInsights(data){
    const recent=data.recentRepositories||[];
    const level=data.activityLevel||"Unknown";

    return `
        <div class="activity-section">
            <div class="activity-header">
                <div>
                    <h3>Recent Activity</h3>
                    <p>Repository activity detected during the recent activity window.</p>
                </div>

                <span class="activity-badge">${level}</span>
            </div>

            ${
                recent.length
                ? `<div class="recent-repository-list">${recent.map((repo,index)=>`
                    <div class="recent-repository-card">
                        <div class="recent-rank">#${index+1}</div>
                        <div class="recent-details">
                            <strong>${escapeHtml(repo.name)}</strong>
                            <span>Last updated: ${formatGitHubDate(repo.pushedAt)}</span>
                        </div>
                    </div>
                `).join("")}</div>`
                : `<p class="activity-empty">No recent repository activity detected.</p>`
            }
        </div>
    `;
}

function createTechnologyProfile(data){
    const percentages=data.languagePercentages||{};
    const languages=Object.entries(percentages);

    return `
        <div id="technologySection" class="technology-profile">
            <div class="technology-header">
                <div>
                    <h3>Technology Profile</h3>
                    <p>Programming language distribution across repositories.</p>
                </div>

                <span class="technology-badge">${languages.length} Languages</span>
            </div>

            ${
                languages.length
                ? `<div class="technology-grid">${languages.map(([language,percentage])=>`
                    <div class="technology-card">
                        <div class="technology-card-header">
                            <strong>${escapeHtml(language)}</strong>
                            <span>${percentage}%</span>
                        </div>

                        <div class="technology-progress">
                            <div class="technology-progress-fill" style="width:${percentage}%"></div>
                        </div>

                        <small>${percentage}% of detected repositories</small>
                    </div>
                `).join("")}</div>`
                : `<p class="activity-empty">No programming languages detected.</p>`
            }
        </div>
    `;
}

function createRepositoryQuality(data){
    return `
        <div id="qualitySection" class="repository-quality">
            <div class="quality-header">
                <div>
                    <h3>Repository Quality</h3>
                    <p>Quality distribution based on repository completeness and popularity signals.</p>
                </div>

                <div class="quality-score">
                    <strong>${data.averageQualityScore||0}</strong>
                    <span>Average</span>
                </div>
            </div>

            <div class="quality-level">
                <span>Overall Quality</span>
                <strong>${data.overallQualityLevel||"Basic"}</strong>
            </div>

            <div class="quality-grid">
                <div class="quality-card">
                    <span>🏆</span>
                    <div>
                        <strong>${data.excellentRepositories||0}</strong>
                        <small>Excellent</small>
                    </div>
                </div>

                <div class="quality-card">
                    <span>👍</span>
                    <div>
                        <strong>${data.goodRepositories||0}</strong>
                        <small>Good</small>
                    </div>
                </div>

                <div class="quality-card">
                    <span>📈</span>
                    <div>
                        <strong>${data.moderateRepositories||0}</strong>
                        <small>Moderate</small>
                    </div>
                </div>

                <div class="quality-card">
                    <span>🌱</span>
                    <div>
                        <strong>${data.basicRepositories||0}</strong>
                        <small>Basic</small>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function createSkillEvidence(data){
    const skills=data.skillEvidence||[];

    return `
        <div id="skillSection" class="skill-evidence">
            <div class="skill-evidence-header">
                <div>
                    <h3>Skill Evidence</h3>
                    <p>Languages supported by actual repositories.</p>
                </div>

                <span class="skill-count">${skills.length} Skills</span>
            </div>

            ${
                skills.length
                ? `<div class="skill-evidence-list">${skills.map((item,index)=>`
                    <div class="skill-evidence-card">
                        <div class="skill-rank">#${index+1}</div>

                        <div class="skill-details">
                            <div class="skill-title-row">
                                <strong>${escapeHtml(item.skill)}</strong>
                                <span>${item.repositoryCount} repositories</span>
                            </div>

                            <p>${escapeHtml(item.evidence)}</p>
                        </div>
                    </div>
                `).join("")}</div>`
                : `<p class="activity-empty">No skill evidence available.</p>`
            }
        </div>
    `;
}

function createTopRepositories(data){
    const repos=data.topRepositories||[];

    return `
        <div class="repository-quality">
            <div class="quality-header">
                <div>
                    <h3>Top Repositories</h3>
                    <p>Repositories with the strongest popularity signals.</p>
                </div>
            </div>

            ${
                repos.length
                ? repos.map((repo,index)=>`
                    <div class="repository-quality-mini">
                        <span>#${index+1} ${escapeHtml(repo.name)}</span>
                        <strong>⭐ ${repo.stars} · 🍴 ${repo.forks}</strong>
                    </div>
                `).join("")
                : `<p class="activity-empty">No repository popularity data available.</p>`
            }
        </div>
    `;
}

function createTargetRoleSection(){
    return `
        <div id="roleSection" class="target-role-section">
            <div class="target-role-header">
                <h3>Target Role Analysis</h3>
                <p>Compare detected GitHub skills with the skills commonly required for a target role.</p>
            </div>

            <div class="target-role-controls">
                <select id="targetRole" class="target-role-select">
                    <option value="">Select a target role</option>
                </select>

                <button class="role-analyze-button" onclick="analyzeTargetRole()">
                    Analyze Role
                </button>
            </div>

            <div id="roleAnalysisResult" class="role-analysis-result"></div>
        </div>
    `;
}

async function loadTargetRoles(){
    try{
        const response=await fetch("/github/roles");
        if(!response.ok) return;

        const roles=await response.json();
        const select=document.getElementById("targetRole");

        if(!select) return;

        roles.forEach(role=>{
            const option=document.createElement("option");
            option.value=role;
            option.textContent=role;
            select.appendChild(option);
        });
    }catch(error){
        console.error(error);
    }
}

async function analyzeTargetRole(){
    const select=document.getElementById("targetRole");
    const result=document.getElementById("roleAnalysisResult");
    const role=select?.value;

    if(!role){
        result.innerHTML=`<div class="role-warning">Please select a target role first.</div>`;
        return;
    }

    result.innerHTML=`
        <div class="role-loading">
            <div class="loader"></div>
            <span>Analyzing target role...</span>
        </div>
    `;

    try{
        const [roleResponse,recommendationResponse,roadmapResponse]=await Promise.all([
            fetch(`/github/${currentUsername}/role-analysis?role=${encodeURIComponent(role)}`),
            fetch(`/github/${currentUsername}/recommendations?role=${encodeURIComponent(role)}`),
            fetch(`/github/${currentUsername}/roadmap?role=${encodeURIComponent(role)}`)
        ]);

        if(!roleResponse.ok) throw new Error("Unable to analyze target role.");
        if(!recommendationResponse.ok) throw new Error("Unable to generate recommendations.");
        if(!roadmapResponse.ok) throw new Error("Unable to generate roadmap.");

        const data=await roleResponse.json();
        const recommendations=await recommendationResponse.json();
        const roadmap=await roadmapResponse.json();

        displayRoleAnalysis(data,recommendations,roadmap);
    }catch(error){
        result.innerHTML=`<div class="role-warning">${error.message}</div>`;
    }
}

function displayRoleAnalysis(data,recommendations,roadmap){
    const result=document.getElementById("roleAnalysisResult");
    const coverage=data.coveragePercentage||0;
    const matched=data.matchedSkills||[];
    const develop=data.skillsToDevelop||[];
    const required=data.requiredSkills||[];

    result.innerHTML=`
        <div class="role-analysis-card">
            <div class="role-summary">
                <div>
                    <span class="role-label">TARGET ROLE</span>
                    <h4>${escapeHtml(data.targetRole)}</h4>
                </div>

                <div class="coverage-circle">
                    <strong>${coverage}%</strong>
                    <span>Coverage</span>
                </div>
            </div>

            <div class="role-coverage">
                <div class="coverage-label">
                    <span>Skill Coverage</span>
                    <strong>${data.matchedSkillCount}/${data.totalRequiredSkills}</strong>
                </div>

                <div class="coverage-bar">
                    <div class="coverage-fill" style="width:${coverage}%"></div>
                </div>
            </div>

            <div class="role-skill-group">
                <div class="role-group-header">
                    <h4>Matched Skills</h4>
                    <span>${matched.length}</span>
                </div>

                ${
                    matched.length
                    ? `<div class="role-skill-list">${matched.map(skill=>`
                        <span class="matched-skill">✓ ${escapeHtml(skill)}</span>
                    `).join("")}</div>`
                    : `<p class="no-skills">No matching skills detected.</p>`
                }
            </div>

            <div class="role-skill-group">
                <div class="role-group-header">
                    <h4>Skills to Develop</h4>
                    <span>${develop.length}</span>
                </div>

                ${
                    develop.length
                    ? `<div class="role-skill-list">${develop.map(skill=>`
                        <span class="develop-skill">＋ ${escapeHtml(skill)}</span>
                    `).join("")}</div>`
                    : `<p class="all-skills">All required skills were detected.</p>`
                }
            </div>

            ${createRecommendations(recommendations)}
            ${createRoadmap(roadmap)}

            <div class="required-skills">
                <h4>Required Skills</h4>

                <div class="required-skill-list">
                    ${required.map(skill=>`
                        <span>${escapeHtml(skill)}</span>
                    `).join("")}
                </div>
            </div>
        </div>
    `;
}

function createRecommendations(data){
    const recommendations=data.recommendedSkills||[];
    const plan=data.learningPlan||[];

    return `
        <div style="margin-top:22px;padding-top:18px;border-top:1px solid #eeeeee">
            <h4 style="margin:0 0 12px;color:#17172b;font-size:14px">
                Developer Recommendations
            </h4>

            ${
                recommendations.length
                ? `
                    <div class="role-skill-list">
                        ${recommendations.map(skill=>`
                            <span class="develop-skill">
                                Learn ${escapeHtml(skill)}
                            </span>
                        `).join("")}
                    </div>

                    <div style="margin-top:12px">
                        ${plan.map((item,index)=>`
                            <div style="padding:10px 12px;margin-bottom:7px;border-radius:8px;background:#fafaff;border:1px solid #eeeeee;color:#666;font-size:11px">
                                <strong style="color:#5b3fd1">
                                    Step ${index+1}:
                                </strong>
                                ${escapeHtml(item)}
                            </div>
                        `).join("")}
                    </div>
                `
                : `<p class="all-skills">No additional skill recommendations.</p>`
            }
        </div>
    `;
}

function createRoadmap(data){
    const roadmap=data.roadmap||[];

    return `
        <div style="margin-top:22px;padding-top:18px;border-top:1px solid #eeeeee">
            <h4 style="margin:0 0 12px;color:#17172b;font-size:14px">
                Skill Roadmap
            </h4>

            ${
                roadmap.length
                ? `
                    <div>
                        ${roadmap.map(step=>`
                            <div style="display:flex;align-items:center;gap:10px;padding:10px 12px;margin-bottom:7px;border-radius:8px;background:${step.completed?"#eaf8ef":"#fff5e8"};border:1px solid ${step.completed?"#ccebd7":"#f3dfbb"}">
                                <strong style="min-width:45px;color:${step.completed?"#218548":"#b66b00"}">
                                    Step ${step.step}
                                </strong>

                                <span style="flex:1;color:#17172b;font-size:11px">
                                    ${escapeHtml(step.skill)}
                                </span>

                                <span style="font-size:10px;color:${step.completed?"#218548":"#b66b00"}">
                                    ${escapeHtml(step.status)}
                                </span>
                            </div>
                        `).join("")}
                    </div>
                `
                : `<p class="all-skills">No roadmap available.</p>`
            }
        </div>
    `;
}

function formatGitHubDate(dateString){
    if(!dateString) return "Unknown";

    const date=new Date(dateString);

    if(Number.isNaN(date.getTime())) return dateString;

    return date.toLocaleDateString();
}

document.getElementById("username")?.addEventListener("keydown",event=>{
    if(event.key==="Enter") analyzeGitHub();
});