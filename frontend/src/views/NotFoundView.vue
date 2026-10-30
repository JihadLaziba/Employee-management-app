<template>
  <div class="not-found">
    <p class="code">404</p>
    <h1>Page not found</h1>
    <p class="text">The page you are looking for does not exist.</p>
    <RouterLink to="/" class="btn btn-primary">Back to home</RouterLink>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 16px;
}
.code {
  font-size: 64px;
  font-weight: 800;
  color: var(--primary);
  margin: 0;
}
h1 {
  margin: 0 0 6px;
}
.text {
  color: var(--text-muted);
  margin: 0 0 20px;
}
</style>
