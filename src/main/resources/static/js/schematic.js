(function () {
    'use strict';

    var currentSort = null;

    // --- table filter / sort ---

    function getTableItems() {
        return Array.from(document.querySelectorAll('#tables [data-table-name]'));
    }

    function applyFilter() {
        var filterInput = document.getElementById('table-filter');
        var term = filterInput ? filterInput.value.toLowerCase() : '';
        var items = getTableItems();
        var visibleCount = 0;

        items.forEach(function (el) {
            var matches = el.dataset.tableName.toLowerCase().indexOf(term) !== -1;
            el.style.display = matches ? '' : 'none';
            if (matches) visibleCount++;
        });

        var noResults = document.getElementById('schematic-no-results');
        if (noResults) {
            noResults.style.display = (term && visibleCount === 0) ? '' : 'none';
        }
    }

    function applySort(direction) {
        currentSort = direction;
        var items = getTableItems();
        if (items.length === 0) return;

        items.sort(function (a, b) {
            var nameA = a.dataset.tableName.toLowerCase();
            var nameB = b.dataset.tableName.toLowerCase();
            var cmp = nameA < nameB ? -1 : nameA > nameB ? 1 : 0;
            return direction === 'asc' ? cmp : -cmp;
        });

        var parent = items[0].parentNode;
        items.forEach(function (el) { parent.appendChild(el); });

        applyFilter();

        document.querySelectorAll('#sort-asc, #sort-desc').forEach(function (btn) {
            btn.classList.remove('active');
        });
        var activeBtn = document.getElementById(direction === 'asc' ? 'sort-asc' : 'sort-desc');
        if (activeBtn) activeBtn.classList.add('active');
    }

    function reapply() {
        if (currentSort) {
            applySort(currentSort);
        } else {
            applyFilter();
        }
    }

    // --- query history ---

    var HISTORY_KEY = 'schematic_query_history';
    var HISTORY_MAX = 50;
    var pendingQuery = null;

    function isStorageAvailable() {
        try {
            var k = '__schematic_storage_test__';
            localStorage.setItem(k, '1');
            localStorage.removeItem(k);
            return true;
        } catch (e) {
            return false;
        }
    }

    function loadHistory() {
        try {
            var raw = localStorage.getItem(HISTORY_KEY);
            if (!raw) return [];
            var parsed = JSON.parse(raw);
            return Array.isArray(parsed) ? parsed : [];
        } catch (e) {
            return [];
        }
    }

    function persistHistory(entries) {
        try {
            localStorage.setItem(HISTORY_KEY, JSON.stringify(entries));
        } catch (e) {
            // BR-006: graceful degradation — ignore storage errors
        }
    }

    function addToHistory(query) {
        var q = query.trim();
        if (!q) return;

        var entries = loadHistory();

        // BR-003: if identical to the most recent entry, leave it at the top unchanged
        if (entries.length > 0 && entries[0] === q) {
            return;
        }

        entries.unshift(q);

        // BR-002: cap at maximum
        if (entries.length > HISTORY_MAX) {
            entries = entries.slice(0, HISTORY_MAX);
        }

        persistHistory(entries);
        renderHistoryDropdown();
    }

    function clearHistory() {
        try {
            localStorage.removeItem(HISTORY_KEY);
        } catch (e) {}
        renderHistoryDropdown();
    }

    function renderHistoryDropdown() {
        var menu = document.getElementById('queryHistoryMenu');
        if (!menu) return;

        var entries = loadHistory();
        while (menu.firstChild) menu.removeChild(menu.firstChild);

        if (entries.length === 0) {
            var li = document.createElement('li');
            var span = document.createElement('span');
            span.className = 'dropdown-item text-muted';
            span.textContent = 'No history yet';
            li.appendChild(span);
            menu.appendChild(li);
            return;
        }

        entries.forEach(function (query) {
            var li = document.createElement('li');
            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'dropdown-item font-monospace text-truncate';
            btn.style.maxWidth = '700px';
            btn.title = query;
            btn.textContent = query;
            btn.addEventListener('click', function () {
                var textarea = document.getElementById('sqlQuery');
                if (textarea) {
                    textarea.value = query;
                    textarea.focus();
                }
            });
            li.appendChild(btn);
            menu.appendChild(li);
        });
    }

    function initHistory() {
        if (!isStorageAvailable()) {
            // BR-006: suppress the history UI for this session
            var controls = document.getElementById('query-history-controls');
            if (controls) controls.style.display = 'none';
            return;
        }

        renderHistoryDropdown();

        var clearBtn = document.getElementById('clearHistoryBtn');
        if (clearBtn) {
            clearBtn.addEventListener('click', clearHistory);
        }

        // Capture query text just before HTMX submits the form
        document.addEventListener('htmx:beforeRequest', function (e) {
            if (e.detail.elt && e.detail.elt.id === 'sqlQueryForm') {
                var textarea = document.getElementById('sqlQuery');
                pendingQuery = textarea ? textarea.value : '';
            }
        });

        // Persist to history after a successful response
        document.addEventListener('htmx:afterRequest', function (e) {
            if (e.detail.elt && e.detail.elt.id === 'sqlQueryForm' && e.detail.successful && pendingQuery) {
                addToHistory(pendingQuery);
                pendingQuery = null;
            }
        });
    }

    // --- init ---

    function init() {
        var filterInput = document.getElementById('table-filter');
        var sortAsc = document.getElementById('sort-asc');
        var sortDesc = document.getElementById('sort-desc');

        if (filterInput) filterInput.addEventListener('input', applyFilter);
        if (sortAsc) sortAsc.addEventListener('click', function () { applySort('asc'); });
        if (sortDesc) sortDesc.addEventListener('click', function () { applySort('desc'); });

        document.addEventListener('htmx:afterSwap', function (e) {
            if (e.detail.target && e.detail.target.id === 'tables') {
                reapply();
            }
        });

        initHistory();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
}());