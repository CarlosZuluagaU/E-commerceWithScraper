document.addEventListener('DOMContentLoaded', () => {
  // --- 1. Elementos HTML ---
  const searchForm = document.getElementById('search-form');
  const searchInput = document.getElementById('product-name-input');
  const searchButton = document.getElementById('search-button');
  const resultsGrid = document.getElementById('results-grid');
  const productCardTemplate = document.getElementById('product-card-template');
  const infoContainer = document.getElementById('info-container');
  const infoMessage = document.getElementById('info-message');
  const loadingSpinner = document.getElementById('loading-spinner');
  const resultsCountDisplay = document.getElementById('results-count');

  // --- 2. Funciones de Utilidad para UI ---

  const showMessage = (type, message, dismissible = true) => {
    if (infoContainer && infoMessage) {
      infoContainer.hidden = false;
      infoMessage.textContent = message;

      // Resetear clases
      infoContainer.className = 'alert alert-dismissible fade show mt-3';

      if (type === 'success') {
        infoContainer.classList.add('alert-success');
      } else if (type === 'error') {
        infoContainer.classList.add('alert-danger');
      } else if (type === 'info') {
        infoContainer.classList.add('alert-info');
      }

      let closeBtn = infoContainer.querySelector('.btn-close');

      if (dismissible) {
        if (!closeBtn) {
          closeBtn = document.createElement('button');
          closeBtn.type = 'button';
          closeBtn.className = 'btn-close';
          closeBtn.setAttribute('data-bs-dismiss', 'alert');
          closeBtn.setAttribute('aria-label', 'Close');
          infoContainer.appendChild(closeBtn);
        }
      } else {
        if (closeBtn) closeBtn.remove();
      }
    }
  };

  const hideMessage = () => {
    if (infoContainer) {
      infoContainer.hidden = true;
      infoMessage.textContent = '';
    }
  };

  const showLoading = () => {
    if (loadingSpinner) loadingSpinner.hidden = false;
    resultsGrid.innerHTML = '';
    resultsCountDisplay.textContent = '';
    hideMessage();
  };

  const hideLoading = () => {
    if (loadingSpinner) loadingSpinner.hidden = true;
  };

  const renderProducts = (products) => {
    resultsGrid.innerHTML = '';

    if (products.length === 0) {
      showMessage('info', `No se encontraron productos para "${searchInput.value.trim()}".`);
      resultsCountDisplay.textContent = '0 resultados';
      return;
    }

    products.forEach(product => {
      const productCardClone = productCardTemplate.content.cloneNode(true);

      const cardWrapper = productCardClone.querySelector('.product-card-wrapper');
      const cardImg = productCardClone.querySelector('.card-img-top');
      const cardTitle = productCardClone.querySelector('.card-title');
      const storeName = productCardClone.querySelector('.store-name strong');
      const cardPrice = productCardClone.querySelector('.card-price');
      const viewOfferLink = productCardClone.querySelector('.btn.stretched-link');

      if (cardImg) {
        cardImg.src = product.imageUrl || 'https://via.placeholder.com/250x180?text=Imagen+No+Disp.';
        cardImg.alt = product.name || 'Producto';
      }

      if (cardTitle) cardTitle.textContent = product.name || 'Nombre de Producto Desconocido';
      if (storeName) storeName.textContent = product.store || 'Tienda Desconocida';
      if (cardPrice) cardPrice.textContent = product.price || 'Precio N/A';
      if (viewOfferLink) viewOfferLink.href = product.productUrl || '#';

      resultsGrid.appendChild(cardWrapper);
    });

    showMessage('success', `Se encontraron ${products.length} resultados.`);
    resultsCountDisplay.textContent = `${products.length} resultados`;
  };

  // --- 3. Lógica Principal de Búsqueda ---

  const handleSearch = async (event) => {
    event.preventDefault();
    event.stopPropagation();

    const query = searchInput.value.trim();

    if (!query) {
      searchForm.classList.add('was-validated');
      hideLoading();
      resultsGrid.innerHTML = '';
      resultsCountDisplay.textContent = '';
      showMessage('info', 'Por favor ingresa un producto para buscar.');
      return;
    }

    searchForm.classList.remove('was-validated');
    showLoading();

    try {
      const response = await fetch(`http://localhost:8080/api/products/search?name=${encodeURIComponent(query)}`);

      if (!response.ok) {
        let errorMessageText = `Error al buscar productos (Estado: ${response.status}).`;
        try {
          const errorData = await response.json();
          if (errorData?.message) {
            errorMessageText = errorData.message;
          } else if (typeof errorData === 'string') {
            errorMessageText = errorData;
          }
        } catch {
          errorMessageText = `Error al buscar productos (Estado: ${response.status} - ${response.statusText}).`;
        }
        throw new Error(errorMessageText);
      }

      const products = await response.json();
      console.log("Productos recibidos:", products);

      renderProducts(products);
    } catch (error) {
      console.error("Error en la búsqueda:", error);
      showMessage('error', `Error en la búsqueda: ${error.message}`);
      resultsGrid.innerHTML = '';
      resultsCountDisplay.textContent = '';
    } finally {
      hideLoading();
    }
  };

  // --- 4. Asignar Eventos ---

  searchForm.addEventListener('submit', handleSearch);

  document.querySelectorAll('.suggestion-link').forEach(link => {
    link.addEventListener('click', (event) => {
      event.preventDefault();
      searchInput.value = event.target.textContent;
      handleSearch(event);
    });
  });

  const themeToggle = document.getElementById('theme-toggle');
  if (themeToggle) {
    themeToggle.addEventListener('click', () => {
      const currentTheme = document.documentElement.getAttribute('data-bs-theme');
      const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-bs-theme', newTheme);
      // localStorage.setItem('theme', newTheme); // Descomenta si quieres guardar la preferencia
    });
  }

  const backToTopButton = document.getElementById('back-to-top');
  if (backToTopButton) {
    window.addEventListener('scroll', () => {
      if (window.scrollY > 300) {
        backToTopButton.classList.add('show');
      } else {
        backToTopButton.classList.remove('show');
      }
    });

    backToTopButton.addEventListener('click', () => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  }
});
